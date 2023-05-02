/**
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

import assertk.*
import assertk.assertions.*
import com.meistercharts.version.MeisterChartsVersion
import org.junit.jupiter.api.Test

/**
 */
class meisterchartsVersionTest {
  @Test
  fun testIt() {
    assertThat(MeisterChartsVersion.version).startsWith("1.")
    assertThat(MeisterChartsVersion.monorepoVersion).startsWith("9.")
    assertThat(MeisterChartsVersion.gitCommit).isNotNull()
    assertThat(MeisterChartsVersion.buildDateDay).isNotNull()
    assertThat(MeisterChartsVersion.versionAsStringVerbose).contains(MeisterChartsVersion.version)
  }
}
