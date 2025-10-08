package it.neckar.open.version

/**
 * Application related information
 *
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

  val gitHash: String = VersionConstants.gitHash

  val gitHashShort: String = VersionConstants.gitHashShort

  /**
   * Verbose version string that contains the git information
   */
  val versionAsStringVerbose: String
    get() {
      return "$version ($gitHash)"
    }
}

/**
 * Returns true if the given version number string contains "-SNAPSHOT"
 */
fun String.isSnapshot(): Boolean {
  return this.contains("-SNAPSHOT")
}
