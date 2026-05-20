package it.neckar.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType

/**
 * Ensures that only one Task runs at a time.
 * Acts as a semaphore.
 */
class NonParallelBuildService : BuildService<BuildServiceParameters.None> {
  override fun getParameters(): BuildServiceParameters.None {
    throw UnsupportedOperationException("Not supported - must not be called")
  }
}

/**
 * Registers a non-parallel build service.
 * Which can be used to ensure that only one task runs at a time.
 */
fun Project.nonParallelBuildService(name: String): Provider<NonParallelBuildService> {
  return gradle.sharedServices.registerIfAbsent(name, NonParallelBuildService::class.java) {
    maxParallelUsages = 1
  }
}

/**
 * Avoids executing tasks of the provided type in parallel.
 */
inline fun <reified TaskType : Task> Project.avoidParallelRunningOfTasks() {
  //Collect alls tasks for all subprojects, as late as possible
  gradle.projectsEvaluated {
    val tasksOfType = subprojects.flatMap { it.tasks.withType<TaskType>() }
    val qualifiedName = TaskType::class.qualifiedName

    logger.info("Found ${tasksOfType.size} tasks of type [$qualifiedName]. Mark them to run after each other")

    val nonParallelBuildService = nonParallelBuildService(qualifiedName + "NonParallelBuildService")

    tasksOfType.forEach { task ->
      task.usesService(nonParallelBuildService)
    }
  }
}
