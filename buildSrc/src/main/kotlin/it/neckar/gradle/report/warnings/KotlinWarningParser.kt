package it.neckar.gradle.report.warnings

import it.neckar.gradle.report.events.RepositoryPaths
import it.neckar.gradle.report.events.moduleBuildScriptPath
import it.neckar.gradle.report.events.toModuleDirectory

/**
 * Shapes a compiler warning into the [KotlinWarning] the Code Quality report is built from.
 *
 * The console format is read by [KotlinDiagnosticParser], which handles both severities; what happens
 * here is the shaping the warnings report needs on top of it — the module and target the finding is
 * attributed to, and the anchor for a diagnostic the compiler reports without a location.
 *
 * Errors are dropped: they never reach the Code Quality report, because a build that produced one is
 * red and its diagnostics belong in `failures.json` instead.
 */
object KotlinWarningParser {

  /**
   * The warning [message] describes, or null when the line is not a compiler warning — an error, or a
   * `WARN` line the Gradle plugin rather than the compiler produced (toolchain notices, deprecation
   * notes), which is not a code finding.
   */
  fun parse(message: String, modulePath: String, target: String, paths: RepositoryPaths): KotlinWarning? {
    val diagnostic = KotlinDiagnosticParser.parse(message, paths) ?: return null
    if (diagnostic.severity != KotlinDiagnosticSeverity.Warning) return null

    return KotlinWarning(
      // A diagnostic the compiler reports without a location is a build-configuration problem, and
      // GitLab Code Quality requires a path per finding — so it is anchored to the module's script.
      filePath = diagnostic.filePath ?: modulePath.toModuleBuildScriptPath(),
      line = diagnostic.line ?: 1,
      column = diagnostic.column ?: 1,
      message = diagnostic.message,
      modulePath = modulePath,
      targets = setOf(target),
      diagnostic = diagnostic.code,
    )
  }
}

/**
 * The module's build script, from its Gradle path: `:internal:open:commons:app` ->
 * `internal/open/commons/app/build.gradle.kts`, the root project -> `build.gradle.kts`.
 */
private fun String.toModuleBuildScriptPath(): String = moduleBuildScriptPath(toModuleDirectory())
