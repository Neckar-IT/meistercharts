package it.neckar.gradle.report.events

import org.gradle.tooling.Failure
import org.gradle.tooling.events.problems.FileLocation
import org.gradle.tooling.events.problems.LineInFileLocation
import org.gradle.tooling.events.problems.Problem
import org.gradle.tooling.events.problems.Severity

/**
 * Reads the findings a failed task reported through Gradle's Problems API off its `Failure` tree.
 *
 * This is the structured channel: a tool that reports through it hands Gradle a file, a line, a
 * severity and an identifier, and none of it has to be recovered from console text. Everything that
 * arrives here is therefore preferred over parsing — the parsers exist only for the tools that offer
 * no such channel, the Kotlin compiler foremost.
 *
 * A problem without a file location is dropped: [Diagnostic] is the "finding in a source file" shape,
 * and a problem about the build itself is already covered by the failure's cause chain.
 */
object GradleProblemDiagnostics {

  /** Which tool the diagnostics are attributed to, since the API does not name one. */
  const val Tool: String = "gradle-problems"

  /**
   * Every locatable problem of [failures] and their causes, outermost first, deduplicated, and cut at
   * [Diagnostic.MaxPerTask] like every other source — a tool reporting through the API can report thousands.
   */
  fun of(failures: List<Failure>, paths: RepositoryPaths): List<Diagnostic> =
    failures.problemsRecursively()
      .mapNotNull { it.toDiagnostic(paths) }
      .distinct()
      .take(Diagnostic.MaxPerTask)

  private fun List<Failure>.problemsRecursively(): List<Problem> =
    flatMap { failure -> failure.problems.orEmpty() + failure.causes.problemsRecursively() }

  private fun Problem.toDiagnostic(paths: RepositoryPaths): Diagnostic? {
    val location = (originLocations.orEmpty() + contextualLocations.orEmpty())
      .filterIsInstance<FileLocation>()
      .firstOrNull() ?: return null

    return Diagnostic(
      tool = Tool,
      severity = definition.severity.toDiagnosticSeverity(),
      filePath = paths.relativize(location.path),
      line = (location as? LineInFileLocation)?.line,
      column = (location as? LineInFileLocation)?.column,
      code = definition.id.name,
      message = contextualLabel?.contextualLabel ?: definition.id.displayName,
      documentationLink = definition.documentationLink?.url,
    )
  }

  /**
   * Compared by the numeric value, not by identity: [Severity] is an open type whose constants a
   * provider may re-implement, and a value a newer Gradle introduced is reported as
   * [DiagnosticSeverity.Warning] rather than dropped — a finding of unknown severity is still a finding.
   */
  private fun Severity.toDiagnosticSeverity(): DiagnosticSeverity = when (severity) {
    Severity.ERROR.severity -> DiagnosticSeverity.Error
    Severity.ADVICE.severity -> DiagnosticSeverity.Advice
    else -> DiagnosticSeverity.Warning
  }
}
