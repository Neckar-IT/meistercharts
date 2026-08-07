package it.neckar.gradle.report.warnings

/**
 * One Kotlin compiler warning, as captured from a `compileKotlin<Target>` task.
 *
 * [filePath] is relative to the repository root, [line] and [column] are 1-based. A diagnostic the
 * compiler reports without a location (`Opt-in requirement marker '…' is unresolved` is the whole
 * observed set) is anchored to the module's `build.gradle.kts` — it is a build-configuration problem,
 * and GitLab Code Quality requires a path per finding.
 *
 * [targets] holds every Kotlin target whose compilation reported this warning. The same `commonMain`
 * diagnostic reaches `compileKotlinJvm`, `compileKotlinJs` and `compileKotlinWasmJs` separately, so
 * findings are merged on [filePath] + [line] + [column] + [message] and the targets collected.
 */
data class KotlinWarning(
  val filePath: String,
  val line: Int,
  val column: Int,
  val message: String,
  val modulePath: String,
  val targets: Set<String>,
) {
  init {
    require(line >= 1) { "line must be 1-based but was $line for $filePath" }
    require(column >= 1) { "column must be 1-based but was $column for $filePath" }
  }

  /**
   * Identifies the finding across the targets that report it. Deliberately excludes [targets] and
   * [modulePath]: two targets of the same module produce the same diagnostic at the same position.
   */
  val deduplicationKey: String
    get() = "$filePath:$line:$column:$message"
}
