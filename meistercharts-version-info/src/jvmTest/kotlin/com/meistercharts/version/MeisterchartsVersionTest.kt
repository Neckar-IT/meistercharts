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

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 */
class MeisterchartsVersionTest {
  @Test
  fun testIt() {
    assertThat(MeisterChartsVersion.version).matches(versionPattern)
    assertThat(MeisterChartsVersion.monorepoVersion).matches(versionPattern)
    assertThat(MeisterChartsVersion.gitHash).isNotNull()
    assertThat(MeisterChartsVersion.versionAsStringVerbose).contains(MeisterChartsVersion.version)
  }

  @Test
  fun testConstants() {
    assertThat(MeisterChartsVersionConstants.version).matches(versionPattern)
  }

  companion object {
    /**
     * What is worth asserting is that the generated constants carry a version at all — pinning the
     * major version instead makes every major bump fail here (`monorepo.version` 9 -> 10 did).
     */
    private val versionPattern: Regex = Regex("""\d+\.\d+\.\d+(-SNAPSHOT)?""")
  }
}
