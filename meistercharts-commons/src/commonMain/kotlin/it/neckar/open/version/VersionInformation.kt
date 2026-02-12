package it.neckar.open.version

/**
 * Application related information.
 *
 * Stable values (version, buildDate) come from generated [VersionConstants].
 * Volatile git info is loaded at runtime from resources (JVM)
 * or returns "unknown" (JS) to avoid unnecessary Kotlin recompilation on every commit.
 */
object VersionInformation {
  /**
   * The version number (main version number of the repository)
   */
  val version: String = VersionConstants.monorepoVersion

  /**
   * Returns true if the current version is a snapshot
   */
  val isSnapshot: Boolean = version.isSnapshot()

  /**
   * The build date (only day - not the time)
   */
  val buildDate: String = VersionConstants.buildDate

  /**
   * The git commit date and time (ISO 8601 format).
   * Loaded at runtime from git.properties (JVM). Returns "unknown" on JS.
   */
  val gitCommitDateTime: String by lazy { resolveGitInfo(GitProperty.CommitDateTime) }

  /**
   * The git hash of the current commit.
   * Loaded at runtime from git.properties (JVM). Returns "unknown" on JS.
   */
  val gitHash: String by lazy { resolveGitInfo(GitProperty.Hash) }

  /**
   * The short git hash of the current commit.
   * Loaded at runtime from git.properties (JVM). Returns "unknown" on JS.
   */
  val gitHashShort: String by lazy { resolveGitInfo(GitProperty.HashShort) }

  /**
   * The git branch name.
   * Loaded at runtime from git.properties (JVM). Returns "unknown" on JS.
   */
  val branch: String by lazy { resolveGitInfo(GitProperty.Branch) }

  /**
   * Verbose version string that contains the git information
   */
  val versionAsStringVerbose: String
    get() {
      return "$version ($gitHash)"
    }
}

/**
 * Resolves a volatile git property at runtime.
 * On JVM, reads from git.properties resource file.
 * On JS, returns "unknown" (no synchronous resource loading in ESM context).
 *
 * [GitProperty] enum is generated from [BuildInfoVars] in buildSrc/ResourcesExt.kt.
 */
internal expect fun resolveGitInfo(property: GitProperty): String

/**
 * Returns true if the given version number string contains "-SNAPSHOT"
 */
fun String.isSnapshot(): Boolean {
  return this.contains("-SNAPSHOT")
}
