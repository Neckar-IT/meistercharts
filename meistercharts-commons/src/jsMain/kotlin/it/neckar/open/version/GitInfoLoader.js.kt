package it.neckar.open.version

/**
 * JS implementation: returns "unknown" for all git properties.
 *
 * Unlike JVM (which loads from a git.properties resource file),
 * Kotlin/JS has no synchronous resource-loading mechanism in ESM context.
 * See #786 for potential future improvements.
 */
internal actual fun resolveGitInfo(property: GitProperty): String {
  return "unknown"
}
