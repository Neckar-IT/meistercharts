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
import it.neckar.open.time.ClockNowProvider
import it.neckar.open.time.VirtualNowProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.time.nowProvider
import it.neckar.open.time.resetNowProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

/**
 *
 */
class SetVirtualNowProviderTest {
  @AfterEach
  fun tearDown() {
    resetNowProvider()
    assertThat(nowProvider).isSameAs(ClockNowProvider)
  }

  @Test
  fun testNow() {
    assertThat(nowProvider).isSameAs(ClockNowProvider)

    val fixedNowProvider = VirtualNowProvider(50000.0)

    assertThat(nowMillis()).isGreaterThan(50000.0)
    nowProvider = fixedNowProvider
    assertThat(nowMillis()).isEqualTo(50000.0)
  }
}
