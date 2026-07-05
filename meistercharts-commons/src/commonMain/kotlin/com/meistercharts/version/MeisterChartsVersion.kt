/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

  val gitHash: String = VersionInformation.gitHash

  val gitHashShort: String = VersionInformation.gitHashShort


  /**
   * Verbose version string that contains the git information
   */
  val versionAsStringVerbose: String
    get() {
      return "$version ($gitHash)"
    }
}
