package it.neckar.open.version

import it.neckar.open.version.VersionConstants

/**
 * Application related information
 *
 */
object VersionInformation {
  /**
   * The version number (main version number of the repository)
   */
  const val version: String = VersionConstants.monorepoVersion

  /**
   * Returns true if the current version is a snapshot
   */
  val isSnapshot: Boolean = version.isSnapshot()


  /**
   * The build date (only day - not the time)
   */
  const val buildDateDay: String = VersionConstants.buildDateDay

  const val gitCommit: String = VersionConstants.gitCommit

  /**
   * Verbose version string that contains the git information
   */
  val versionAsStringVerbose: String
    get() {
      return "$version ($gitCommit)"
    }
}

/**
 * Returns true if the given version number string contains "-SNAPSHOT"
 */
fun String.isSnapshot(): Boolean {
  return this.contains("-SNAPSHOT")
}
