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
package com.meistercharts.algorithms.axis

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.TimeZone
import it.neckar.open.unit.si.ms
import korlibs.time.DateTime
import org.junit.jupiter.api.Test

/**
 *
 */
class KlockExtensionsKtTest {
  @Test
  fun testIt() {
    val now: @ms Double = 1.5900732415E12
    assertThat(now.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")

    val dateTimeTz = DateTime(now).utc2DateTimeTz(TimeZone.Berlin)

    assertThat(dateTimeTz.hours).isEqualTo(17)
    assertThat(dateTimeTz.minutes).isEqualTo(0)
    assertThat(dateTimeTz.seconds).isEqualTo(41)
    assertThat(dateTimeTz.minutesOfDay).isEqualTo(17 * 60)
    assertThat(dateTimeTz.secondsOfDay).isEqualTo(17 * 60 * 60 + 41)
  }
}
