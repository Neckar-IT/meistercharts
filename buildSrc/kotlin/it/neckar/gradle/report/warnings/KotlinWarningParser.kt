package it.neckar.gradle.report.warnings

import java.io.File

/**
 * Turns one `WARN`-level line of a Kotlin compilation into a [KotlinWarning].
 *
 * The Kotlin Gradle plugin routes compiler diagnostics through the Gradle logger as formatted text,
 * so the position has to be read from the message — there is no structured diagnostic channel. The
 * format is the compiler's own and fixed:
 *
 * ```
 * w: file:///abs/path/Foo.kt:39:18 [USELESS_CAST] No cast needed.
 * w: file:///abs/path/Foo.kt:39:18 Return type must be specified in explicit API mode.
 * w: Opt-in requirement marker 'kotlin.io.path.ExperimentalPathApi' is unresolved. …
 * ```
 *
 * The bracketed name comes from `-Xrender-internal-diagnostic-names` and is split off into
 * [KotlinWarning.diagnostic]; the message keeps the compiler's own text.
 */
object KotlinWarningParser {
  // Both patterns are used with matchEntire, which anchors on its own. DOT_MATCHES_ALL keeps a
  // diagnostic whose message wraps over several lines in one finding.
  /** `w: file://<absolute path>:<line>:<column> <message>` */
  private val LocatedWarningPattern = Regex("""w:\s+file://(\S+?):(\d+):(\d+)\s+(.*)""", RegexOption.DOT_MATCHES_ALL)

  /** `w: <message>` — a diagnostic the compiler reports for the compilation as a whole. */
  private val UnlocatedWarningPattern = Regex("""w:\s+(.*)""", RegexOption.DOT_MATCHES_ALL)

  /**
   * `[DIAGNOSTIC_NAME] <message>`. The compiler builds the names from upper-case letters, digits and
   * underscores; anchoring on that shape keeps a message that merely starts with a bracketed word —
   * a quoted annotation, a code sample — out of the diagnostic field.
   */
  private val DiagnosticNamePattern = Regex("""\[([A-Z][A-Z0-9_]*)]\s+(.*)""", RegexOption.DOT_MATCHES_ALL)

  /**
   * The finding [message] describes, or null when the line is not a compiler diagnostic. A `WARN`
   * line on a Kotlin compile task that carries no `w:` prefix comes from the Gradle plugin rather
   * than the compiler (toolchain notices, deprecation notes) and is not a code finding.
   */
  fun parse(message: String, modulePath: String, target: String, repositoryRoot: File): KotlinWarning? {
    val trimmed = message.trim()

    LocatedWarningPattern.matchEntire(trimmed)?.let { match ->
      val (absolutePath, line, column, text) = match.destructured
      val diagnostic = text.splitDiagnosticName()

      return KotlinWarning(
        filePath = relativizeToRepository(absolutePath, repositoryRoot),
        line = line.toInt(),
        column = column.toInt(),
        message = diagnostic.message,
        modulePath = modulePath,
        targets = setOf(target),
        diagnostic = diagnostic.name,
      )
    }

    UnlocatedWarningPattern.matchEntire(trimmed)?.let { match ->
      val diagnostic = match.groupValues[1].splitDiagnosticName()

      return KotlinWarning(
        filePath = modulePath.toModuleBuildScriptPath(),
        line = 1,
        column = 1,
        message = diagnostic.message,
        modulePath = modulePath,
        targets = setOf(target),
        diagnostic = diagnostic.name,
      )
    }

    return null
  }

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

  /**
   * Repository-relative path for the report. An absolute path outside the repository — a compiler
   * diagnostic on a dependency's source — is kept as is rather than turned into a `../..` chain that
   * resolves to nothing in the GitLab diff.
   */
  private fun relativizeToRepository(absolutePath: String, repositoryRoot: File): String {
    val rootPrefix = "${repositoryRoot.absolutePath.trimEnd('/')}/"
    return absolutePath.removePrefix(rootPrefix)
  }
}

/**
 * Repository-relative path of a module's build script:
 * `:internal:open:commons:app` -> `internal/open/commons/app/build.gradle.kts`, the root project ->
 * `build.gradle.kts`.
 */
private fun String.toModuleBuildScriptPath(): String {
  val directory = trim(':').replace(':', '/')

  return if (directory.isEmpty()) ModuleBuildScriptFileName else "$directory/$ModuleBuildScriptFileName"
}

private const val ModuleBuildScriptFileName: String = "build.gradle.kts"
