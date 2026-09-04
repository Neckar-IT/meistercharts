package it.neckar.gradle.report.events

import it.neckar.gradle.report.warnings.KotlinDiagnosticParser
import it.neckar.gradle.report.warnings.KotlinDiagnosticSeverity
import java.io.File

/**
 * Reads a failed compile task's errors out of the log the build captured for it.
 *
 * The Kotlin compiler reports nothing through Gradle's Problems API — its diagnostics exist only as
 * the `e:` lines it prints. Those lines are already in the task's own log
 * (`build-reports/logs/<module>/<task>.log`, written by [TaskLogWriter]), attributed to exactly this
 * task, so reading them there needs no second capture and no cross-plugin wiring. The format is read
 * in one place, [KotlinDiagnosticParser], with a test.
 *
 * Errors only. A warning of a task that failed is not what broke it, and the warnings have their own
 * report (`build-reports/kotlin-warnings.json`).
 */
object KotlinLogDiagnostics {

  const val Tool: String = "kotlinc"

  /**
   * The compiler errors in [logFile], or empty when it is absent — which is every task that produced
   * no output, and every task that is not a Kotlin compilation.
   *
   * [modulePath] is the repository-relative directory of the module being compiled; it anchors a
   * diagnostic the compiler reported without a file of its own. [paths] turns the absolute path the
   * compiler prints into the repository-relative one the report carries.
   */
  fun of(logFile: File?, modulePath: String?, paths: RepositoryPaths): List<Diagnostic> {
    if (logFile == null || logFile.isFile.not()) return emptyList()

    // Streamed and cut at [Diagnostic.MaxPerTask] rather than read whole: a compilation that logged
    // megabytes is exactly where the report writer must not be the second thing to run out of memory.
    return logFile.useLines { lines ->
      lines
        .mapNotNull { KotlinDiagnosticParser.parse(it, paths) }
        .filter { it.severity == KotlinDiagnosticSeverity.Error }
        .distinct()
        .take(Diagnostic.MaxPerTask)
        .toList()
    }
      .map { parsed ->
        Diagnostic(
          tool = Tool,
          severity = DiagnosticSeverity.Error,
          filePath = parsed.filePath ?: moduleBuildScriptPath(modulePath),
          line = parsed.line,
          column = parsed.column,
          code = parsed.code,
          message = parsed.message,
        )
      }
  }

}
