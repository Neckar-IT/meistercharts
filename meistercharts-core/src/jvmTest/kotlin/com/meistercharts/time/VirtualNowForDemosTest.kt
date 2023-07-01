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
package com.meistercharts.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.VirtualNowProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Test

class VirtualNowForDemosTest {
  @Test
  fun testIt() {
    @ms val fixedStart = 17788887777777.0
    assertThat(fixedStart.formatUtc()).isEqualTo("2533-09-15T21:42:57.777")

    @ms val realNow = nowMillis()
    @ms val deltaToReal = fixedStart - realNow

    val virtualNowProvider = VirtualNowProvider(fixedStart)

    assertThat(virtualNowProvider.nowMillis()).isEqualTo(fixedStart)



  }
}
