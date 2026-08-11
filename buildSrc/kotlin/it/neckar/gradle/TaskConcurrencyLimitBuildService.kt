package it.neckar.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType

/**
 * Semaphore that caps how many tasks run at the same time.
 * The cap is the service's `maxParallelUsages`; each task holding it counts as one usage.
 */
class TaskConcurrencyLimitBuildService : BuildService<BuildServiceParameters.None> {
  override fun getParameters(): BuildServiceParameters.None {
    throw UnsupportedOperationException("Not supported - must not be called")
  }
}

/**
 * Registers a semaphore that admits at most [maxParallelUsages] holders at a time.
 */
fun Project.taskConcurrencyLimitBuildService(name: String, maxParallelUsages: Int): Provider<TaskConcurrencyLimitBuildService> {
  require(maxParallelUsages >= 1) { "maxParallelUsages must be >= 1 but was <$maxParallelUsages> for service <$name>" }

  return gradle.sharedServices.registerIfAbsent(name, TaskConcurrencyLimitBuildService::class.java) {
    this.maxParallelUsages = maxParallelUsages
  }
}

/**
 * Runs at most [maxParallelUsages] tasks of [TaskType] at a time, across every project of the build.
 * Every other task keeps the full `org.gradle.workers.max` parallelism.
 *
 * Two calls for the same [TaskType] with different limits yield two semaphores and each task holds
 * both, so the stricter limit wins.
 */
inline fun <reified TaskType : Task> Project.limitParallelRunningOfTasks(maxParallelUsages: Int) {
  require(this == rootProject) { "limitParallelRunningOfTasks covers every project of the build and belongs in the root project, but was called on <$path>" }

  val qualifiedName = TaskType::class.qualifiedName

  // The limit is part of the service name: one service per (type, limit), so a second call with a
  // different limit gets its own semaphore instead of silently reusing the first one's cap.
  val concurrencyLimit = taskConcurrencyLimitBuildService("$qualifiedName.ConcurrencyLimit.$maxParallelUsages", maxParallelUsages)

  // allprojects + configureEach, never subprojects + flatMap: the latter realizes every task object
  // of the type on every build, even when none of them runs. It also skips the root project.
  allprojects {
    tasks.withType<TaskType>().configureEach {
      logger.info("Limiting [$path] to [$maxParallelUsages] parallel executions")
      usesService(concurrencyLimit)
    }
  }
}

/**
 * Avoids executing tasks of the provided type in parallel.
 */
inline fun <reified TaskType : Task> Project.avoidParallelRunningOfTasks() {
  limitParallelRunningOfTasks<TaskType>(1)
}
