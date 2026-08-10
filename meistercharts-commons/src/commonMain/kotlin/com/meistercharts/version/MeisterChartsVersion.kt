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
 * Version information about MeisterCharts.
 *
 * MeisterCharts is released on its own schedule, independently of the monorepo, and therefore has
 * its own version line: at the time of writing [version] is `1.39.0-SNAPSHOT` while
 * [monorepoVersion] is `10.1.0-SNAPSHOT` — not even the major versions line up. Whatever a
 * MeisterCharts artifact reports about itself — the npm package version, the demo titles, the debug
 * layer — must use [version]; [VersionInformation] alone would report the monorepo version and be
 * wrong here.
 *
 * The git values are passed through from [VersionInformation] unchanged: they describe the commit
 * the whole repository was built from, and they are [VersionInformation.UnknownGitValue] wherever
 * no deploy metadata is injected (local development, plain tests).
 */
object MeisterChartsVersion {
  /**
   * The MeisterCharts version, e.g. `1.39.0-SNAPSHOT`.
   *
   * Read from the generated [MeisterChartsVersionConstants], which this module's
   * `createVersionConstants` Gradle task writes from the root `meistercharts.version` file. Not the
   * monorepo version — see [monorepoVersion].
   */
  val version: String = MeisterChartsVersionConstants.version

  /**
   * Whether [version] is a development version rather than a release — this is about the
   * MeisterCharts version line, not the monorepo one ([VersionInformation.isSnapshot]).
   *
   * True in every build that is not a release: `meistercharts.version` is bumped for the release
   * only and the bump is never merged back, and `verifyMeisterchartsVersionIsDevelopment` fails a
   * merge request that carries a non-SNAPSHOT version.
   */
  val isSnapshot: Boolean = version.isSnapshot()

  /**
   * The version of the monorepo this build was compiled in, e.g. `10.1.0-SNAPSHOT`. It moves with
   * monorepo releases, which have nothing to do with MeisterCharts releases — for anything that
   * identifies MeisterCharts itself use [version].
   */
  val monorepoVersion: String = VersionInformation.version

  /**
   * The full git hash of the commit the repository was built from, or
   * [VersionInformation.UnknownGitValue] when nothing injected it (local development, plain tests).
   */
  val gitHash: String = VersionInformation.gitHash

  /**
   * The shortened form of [gitHash] (see [VersionInformation.gitHashShort]), or
   * [VersionInformation.UnknownGitValue] when the hash is unresolved.
   */
  val gitHashShort: String = VersionInformation.gitHashShort


  /**
   * The MeisterCharts version plus the git hash, for display only — log lines, demo window titles,
   * the debug version layer.
   *
   * It deliberately combines the two sources: [version] says which MeisterCharts release this is,
   * [gitHash] which repository state it was built from. Where the hash is unresolved the string
   * reads `1.39.0-SNAPSHOT (unknown)`, so nothing may parse it.
   */
  val versionAsStringVerbose: String
    get() {
      return "$version ($gitHash)"
    }
}
