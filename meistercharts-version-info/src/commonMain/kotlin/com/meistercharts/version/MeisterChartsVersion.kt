package com.meistercharts.version

import it.neckar.open.version.VersionInformation
import it.neckar.open.version.isSnapshot

/**
 * Application related information
 *
 */
object MeisterChartsVersion {
  /**
   * The version number of MeisterCharts
   */
  val version: String = MeisterChartsVersionConstants.version

  /**
   * Returns true if the current MeisterCharts version is a snapshot
   */
  val isSnapshot: Boolean = version.isSnapshot()

  val monorepoVersion: String = VersionInformation.version

  /**
   * The build date (only day - not the time)
   */
  val buildDateDay: String = VersionInformation.buildDateDay

  val gitCommit: String = VersionInformation.gitCommit


  /**
   * Verbose version string that contains the git information
   */
  val versionAsStringVerbose: String
    get() {
      return "$version ($gitCommit)"
    }
}
