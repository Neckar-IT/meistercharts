package it.neckar.gradle.report.warnings

import it.neckar.gradle.report.events.RepositoryPaths

/**
 * Turns one line of Kotlin compiler output into a [ParsedKotlinDiagnostic] — the single place in the
 * build where the compiler's console format is read.
 *
 * The Kotlin Gradle plugin routes compiler diagnostics through the Gradle logger as formatted text and
 * exposes no structured channel, so the severity, the position and the diagnostic name exist only in
 * the text. The format is the compiler's own and fixed:
 *
 * ```
 * w: file:///abs/path/Foo.kt:39:18 [USELESS_CAST] No cast needed.
 * e: file:///abs/path/Foo.kt:48:25 [UNRESOLVED_REFERENCE] Unresolved reference 'Foo'.
 * w: Opt-in requirement marker 'kotlin.io.path.ExperimentalPathApi' is unresolved. …
 * ```
 *
 * Both severities are parsed here. [KotlinWarningParser] keeps the warnings for the Code Quality
 * report; the errors go into `failures.json` as
 * [it.neckar.gradle.report.events.Diagnostic] entries of the failing compile task.
 */
object KotlinDiagnosticParser {

  // Both patterns are used with matchEntire, which anchors on its own. DOT_MATCHES_ALL keeps a
  // diagnostic whose message wraps over several lines in one finding.
  /** `<severity>: file://<absolute path>:<line>:<column> <message>` */
  private val LocatedPattern = Regex("""([we]):\s+file://(\S+?):(\d+):(\d+)\s+(.*)""", RegexOption.DOT_MATCHES_ALL)

  /** `<severity>: <message>` — a diagnostic the compiler reports for the compilation as a whole. */
  private val UnlocatedPattern = Regex("""([we]):\s+(.*)""", RegexOption.DOT_MATCHES_ALL)

  /**
   * `[DIAGNOSTIC_NAME] <message>`. The compiler builds the names from upper-case letters, digits and
   * underscores; anchoring on that shape keeps a message that merely starts with a bracketed word —
   * a quoted annotation, a code sample — out of the diagnostic field.
   */
  private val DiagnosticNamePattern = Regex("""\[([A-Z][A-Z0-9_]*)]\s+(.*)""", RegexOption.DOT_MATCHES_ALL)

  /**
   * The diagnostic [message] carries, or `null` when the line is not compiler output. A line on a
   * Kotlin compile task without a `w:` or `e:` prefix comes from the Gradle plugin rather than the
   * compiler (toolchain notices, deprecation notes) and is not a code finding.
   *
   * [paths] relativizes the path the compiler prints. A path outside the repository is kept absolute
   * rather than turned into a `../..` chain that resolves to nothing in a GitLab diff.
   */
  fun parse(message: String, paths: RepositoryPaths): ParsedKotlinDiagnostic? {
    val trimmed = message.trim()

    LocatedPattern.matchEntire(trimmed)?.let { match ->
      val (severity, absolutePath, line, column, text) = match.destructured
      val named = text.splitDiagnosticName()

      return ParsedKotlinDiagnostic(
        severity = severity.toSeverity(),
        filePath = paths.relativize(absolutePath),
        line = line.toInt(),
        column = column.toInt(),
        code = named.name,
        message = named.message,
      )
    }

    UnlocatedPattern.matchEntire(trimmed)?.let { match ->
      val named = match.groupValues[2].splitDiagnosticName()

      return ParsedKotlinDiagnostic(
        severity = match.groupValues[1].toSeverity(),
        filePath = null,
        line = null,
        column = null,
        code = named.name,
        message = named.message,
      )
    }

    return null
  }

  private fun String.toSeverity(): KotlinDiagnosticSeverity =
    if (this == "e") KotlinDiagnosticSeverity.Error else KotlinDiagnosticSeverity.Warning

  /**
   * Separates the leading `[DIAGNOSTIC_NAME]` from the message text. A message without the prefix keeps
   * its full text and reports no name — the compiler renders the names only under
   * `-Xrender-internal-diagnostic-names`, and not every diagnostic carries one even then.
   */
  private fun String.splitDiagnosticName(): NamedDiagnostic {
    val trimmed = trim()

    return DiagnosticNamePattern.matchEntire(trimmed)
      ?.let { NamedDiagnostic(name = it.groupValues[1], message = it.groupValues[2].trim()) }
      ?: NamedDiagnostic(name = null, message = trimmed)
  }

  private data class NamedDiagnostic(val name: String?, val message: String)
}

/**
 * One compiler diagnostic as it stands in the console output, before either consumer shapes it.
 *
 * [filePath] is repository-relative and `null` for a diagnostic the compiler reports about the
 * compilation as a whole; [line] and [column] are 1-based and `null` with it. [code] is the compiler's
 * internal diagnostic name — `USELESS_CAST`, `UNRESOLVED_REFERENCE` — rendered by
 * `-Xrender-internal-diagnostic-names`, and `null` for a diagnostic that carries none.
 */
data class ParsedKotlinDiagnostic(
  val severity: KotlinDiagnosticSeverity,
  val filePath: String?,
  val line: Int?,
  val column: Int?,
  val code: String?,
  val message: String,
)

/** The severity the compiler's own `w:` / `e:` prefix states. */
enum class KotlinDiagnosticSeverity {
  Warning,
  Error,
}
