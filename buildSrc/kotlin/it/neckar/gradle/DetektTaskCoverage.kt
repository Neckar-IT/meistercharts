package it.neckar.gradle

/**
 * Which of a project's Detekt tasks actually add analysis coverage.
 *
 * Detekt registers up to three analyses over the same files: the per-compilation task with type
 * resolution (`detektMain`), the per-source-set task without it (`detektMainSourceSet`), and the
 * project-wide `detekt`. The compilation task runs a **superset** of the rules over the same
 * sources — `RequiresAnalysisApi` rules fire only there, every other rule fires in both — so
 * whatever a compilation task already covers is duplicate work. Measured on
 * `:internal:patterns:ktor-backend`: 1579 file analyses where 582 produce the same findings.
 *
 * What must survive the selection are the source sets no compilation task sees: a multiplatform
 * module registers compilation tasks only for its JVM target, so `detektJsMainSourceSet` is the
 * only analysis its `jsMain` sources ever get.
 */
object DetektTaskCoverage {
  /** Detekt's project-wide task — no type resolution, sources are the union of the main source sets. */
  private const val ProjectWideTaskName: String = "detekt"

  /** Suffix of Detekt's per-source-set tasks, which run without type resolution. */
  private const val SourceSetTaskSuffix: String = "SourceSet"

  /** One Detekt task: its name and the source files it analyses. */
  data class TaskSources(val name: String, val sourceFiles: Set<String>)

  /**
   * The names of the tasks in [tasks] that add coverage: every compilation task, plus the tasks
   * holding a file no compilation task analyses.
   *
   * Fail-safe: a project whose tasks contain no recognizable compilation task keeps all of them —
   * a Detekt release that renames its tasks costs duplicate work, never lost analysis.
   */
  fun namesAddingCoverage(tasks: List<TaskSources>): List<String> {
    val compilationTasks = tasks.filter { isCompilationTask(it.name) }
    if (compilationTasks.isEmpty()) {
      return tasks.map { it.name }
    }

    val compiledFiles: Set<String> = compilationTasks.flatMapTo(HashSet()) { it.sourceFiles }
    val uncoveredTasks = tasks
      .filterNot { isCompilationTask(it.name) }
      .filter { task -> task.sourceFiles.any { it !in compiledFiles } }

    return (compilationTasks + uncoveredTasks).map { it.name }
  }

  /**
   * Detekt names a per-compilation task after its compilation (`detektMain`, `detektMainJvm`); the
   * two task kinds without type resolution are the project-wide [ProjectWideTaskName] and the
   * per-source-set tasks ending in [SourceSetTaskSuffix].
   */
  private fun isCompilationTask(taskName: String): Boolean {
    return taskName != ProjectWideTaskName && taskName.endsWith(SourceSetTaskSuffix).not()
  }
}
