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

import assertk.*
import assertk.assertions.*
import com.meistercharts.axis.DistanceMillis
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.TimeZone
import org.junit.jupiter.api.Test

class DistanceMillisTest {
  @Test
  fun testIndex() {
    val value0 = 1.000008359E12
    val value1 = 1.00000836E12

    assertThat(value0.formatUtc()).isEqualTo("2001-09-09T04:05:59.000")
    assertThat(value1.formatUtc()).isEqualTo("2001-09-09T04:06:00.000")

    assertThat(DistanceMillis(1000.0).calculateEstimatedIndex(value0, TimeZone.Berlin).value).isEqualTo(1000008359)
    assertThat(DistanceMillis(1000.0).calculateEstimatedIndex(value1, TimeZone.Berlin).value).isEqualTo(1000008360)
  }

  @Test
  fun testIndex2() {
    val value0 = 1.0000007999E12
    val value1 = 1.0000008E12

    assertThat(value0 / 100).isEqualTo(1.0000007999E10)
    assertThat(value1 / 100).isEqualTo(1.0000008E10)
    assertThat(((value0 / 100) % 2147483647).toInt()).isEqualTo(1410073411)
    assertThat(((value1 / 100) % 2147483647).toInt()).isEqualTo(1410073412)

    assertThat(value0.formatUtc()).isEqualTo("2001-09-09T01:59:59.900")
    assertThat(value1.formatUtc()).isEqualTo("2001-09-09T02:00:00.000")

    assertThat(DistanceMillis(100.0).calculateEstimatedIndex(value0, TimeZone.Berlin).value).isEqualTo(1410073411)
    assertThat(DistanceMillis(100.0).calculateEstimatedIndex(value1, TimeZone.Berlin).value).isEqualTo(1410073412)
  }
}
