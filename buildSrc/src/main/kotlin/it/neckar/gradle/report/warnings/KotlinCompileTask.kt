package it.neckar.gradle.report.warnings

/**
 * Recognises a Kotlin compile task by its path and names the target it compiles for.
 *
 * This is the structural half of the warning filter: together with `logLevel == WARN` it decides that
 * a log event is a Kotlin compiler diagnostic, without looking at the message.
 *
 * The Kotlin Gradle plugin spells the task name `compile[<Scope>]Kotlin[<Target>]`, which covers every
 * form the build produces:
 *
 * | Task | Target |
 * |---|---|
 * | `compileKotlinJvm`, `compileKotlinLinuxX64` | `Jvm`, `LinuxX64` |
 * | `compileTestKotlinJvm` | `Jvm` — a warning in test code is a warning |
 * | `compileKotlin`, `compileTestKotlin` (Kotlin/JVM module, no target suffix) | `Jvm` |
 * | `compileKotlinMetadata`, `compileCommonMainKotlinMetadata` | `Metadata` |
 */
object KotlinCompileTask {

  private val TaskNamePattern = Regex("""^compile([A-Z]\w*?)?Kotlin([A-Z]\w*)?$""")

  /** Target of a task name without a suffix: a Kotlin/JVM module compiles for the JVM. */
  private const val DefaultTargetName: String = "Jvm"

  /**
   * The Kotlin target [taskPath] compiles for, or null when the task is not a Kotlin compilation.
   */
  fun targetOrNull(taskPath: String): String? {
    val match = TaskNamePattern.matchEntire(taskPath.substringAfterLast(':')) ?: return null

    return match.groupValues[2].ifEmpty { DefaultTargetName }
  }
}
