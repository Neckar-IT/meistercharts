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
 * w: file:///abs/path/Foo.kt:39:18 Return type must be specified in explicit API mode.
 * w: Opt-in requirement marker 'kotlin.io.path.ExperimentalPathApi' is unresolved. …
 * ```
 */
object KotlinWarningParser {
  // Both patterns are used with matchEntire, which anchors on its own. DOT_MATCHES_ALL keeps a
  // diagnostic whose message wraps over several lines in one finding.
  /** `w: file://<absolute path>:<line>:<column> <message>` */
  private val LocatedWarningPattern = Regex("""w:\s+file://(\S+?):(\d+):(\d+)\s+(.*)""", RegexOption.DOT_MATCHES_ALL)

  /** `w: <message>` — a diagnostic the compiler reports for the compilation as a whole. */
  private val UnlocatedWarningPattern = Regex("""w:\s+(.*)""", RegexOption.DOT_MATCHES_ALL)

  /**
   * The finding [message] describes, or null when the line is not a compiler diagnostic. A `WARN`
   * line on a Kotlin compile task that carries no `w:` prefix comes from the Gradle plugin rather
   * than the compiler (toolchain notices, deprecation notes) and is not a code finding.
   */
  fun parse(message: String, modulePath: String, target: String, repositoryRoot: File): KotlinWarning? {
    val trimmed = message.trim()

    LocatedWarningPattern.matchEntire(trimmed)?.let { match ->
      val (absolutePath, line, column, text) = match.destructured

      return KotlinWarning(
        filePath = relativizeToRepository(absolutePath, repositoryRoot),
        line = line.toInt(),
        column = column.toInt(),
        message = text.trim(),
        modulePath = modulePath,
        targets = setOf(target),
      )
    }

    UnlocatedWarningPattern.matchEntire(trimmed)?.let { match ->
      return KotlinWarning(
        filePath = modulePath.toModuleBuildScriptPath(),
        line = 1,
        column = 1,
        message = match.groupValues[1].trim(),
        modulePath = modulePath,
        targets = setOf(target),
      )
    }

    return null
  }

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
