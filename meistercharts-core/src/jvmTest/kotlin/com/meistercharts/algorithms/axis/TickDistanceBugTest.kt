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
import com.meistercharts.axis.DistanceSeconds
import com.meistercharts.time.utc2DateTimeTz
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.TimeZone
import it.neckar.open.time.toDoubleMillis
import korlibs.time.DateTime
import org.junit.jupiter.api.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class TickDistanceBugTest {
  @Test
  fun testIAE() {
    val startDate = ZonedDateTime.of(2022, 8, 5, 12, 19, 59, TimeUnit.MILLISECONDS.toNanos(999L).toInt(), ZoneOffset.UTC)
    val endDate = ZonedDateTime.of(2022, 8, 5, 12, 22, 49, TimeUnit.MILLISECONDS.toNanos(349L).toInt(), ZoneOffset.UTC)

    val ticks = DistanceSeconds(10).calculateTicks(startDate.toDoubleMillis(), endDate.toDoubleMillis(), TimeZone.Berlin, true)

    assertThat(ticks.size).isGreaterThan(2)
    assertThat(ticks.first().formatUtc()).isEqualTo("2022-08-05T12:20:00.000Z")
  }

  @Test
  fun testReproduce() {
    val startDate = ZonedDateTime.of(2022, 8, 5, 12, 20, 0, 0, ZoneOffset.UTC).minusNanos(123)
    val endDate = ZonedDateTime.of(2022, 8, 5, 12, 22, 49, TimeUnit.MILLISECONDS.toNanos(349L).toInt(), ZoneOffset.UTC)

    val startMillis = startDate.toDoubleMillis()
    assertThat(startMillis.formatUtc()).isEqualTo("2022-08-05T12:19:59.999Z")

    val startKlock = DateTime(startMillis).utc2DateTimeTz(TimeZone.Berlin)
    assertThat(startKlock.milliseconds).isEqualTo(0)
    assertThat(startKlock.seconds).isEqualTo(0)


    val ticks = DistanceSeconds(10).calculateTicks(startMillis, endDate.toDoubleMillis(), TimeZone.Berlin, true)
    assertThat(ticks.first().formatUtc()).isEqualTo("2022-08-05T12:20:00.000Z")
  }
}

