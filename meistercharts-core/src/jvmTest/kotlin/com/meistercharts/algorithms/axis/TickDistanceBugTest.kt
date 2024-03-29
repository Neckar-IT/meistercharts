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
import com.meistercharts.axis.time.DistanceHours
import com.meistercharts.axis.time.DistanceSeconds
import it.neckar.datetime.minimal.TimeZone
import it.neckar.open.collections.first
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.WithTimeZone
import it.neckar.open.time.toDoubleMillis
import org.junit.jupiter.api.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@WithTimeZone("Europe/Berlin")
class TickDistanceBugTest {
  @Test
  fun testIAE() {
    val startDate = ZonedDateTime.of(2022, 8, 5, 12, 19, 59, TimeUnit.MILLISECONDS.toNanos(999L).toInt(), ZoneOffset.UTC)
    val endDate = ZonedDateTime.of(2022, 8, 5, 12, 22, 49, TimeUnit.MILLISECONDS.toNanos(349L).toInt(), ZoneOffset.UTC)

    val ticks = DistanceSeconds(10).calculateTicks(startDate.toDoubleMillis(), endDate.toDoubleMillis(), TimeZone.Berlin)

    assertThat(ticks.size).isGreaterThan(2)
    assertThat(ticks[0].formatUtc()).isEqualTo("2022-08-05T12:19:50.000Z")
    assertThat(ticks[1].formatUtc()).isEqualTo("2022-08-05T12:20:00.000Z")
  }

  @Test
  fun testReproduce() {
    val startDate = ZonedDateTime.of(2022, 8, 5, 12, 20, 0, 0, ZoneOffset.UTC).minusNanos(123)
    val endDate = ZonedDateTime.of(2022, 8, 5, 12, 22, 49, TimeUnit.MILLISECONDS.toNanos(349L).toInt(), ZoneOffset.UTC)

    val startMillis = startDate.toDoubleMillis()
    assertThat(startMillis.formatUtc()).isEqualTo("2022-08-05T12:19:59.999Z")

    val ticks = DistanceSeconds(10).calculateTicks(startMillis, endDate.toDoubleMillis(), TimeZone.Berlin)
    assertThat(ticks[0].formatUtc()).isEqualTo("2022-08-05T12:19:50.000Z")
    assertThat(ticks[1].formatUtc()).isEqualTo("2022-08-05T12:20:00.000Z")
  }

  @Test
  fun testBugHours1() {
    val startDate = ZonedDateTime.of(2023,12,31,15,16,19, 0, ZoneOffset.UTC).minusNanos(123)
    val endDate = ZonedDateTime.of(2024,1,1,13,53,57, TimeUnit.MILLISECONDS.toNanos(349L).toInt(), ZoneOffset.UTC)

    val startMillis = startDate.toDoubleMillis()
    assertThat(startMillis.formatUtc()).isEqualTo("2023-12-31T15:16:18.999Z")

    val ticks = DistanceHours(3).calculateTicks(startMillis, endDate.toDoubleMillis(), TimeZone.Berlin)
    assertThat(ticks[0].formatUtc()).isEqualTo("2023-12-31T14:00:00.000Z")
    assertThat(ticks[1].formatUtc()).isEqualTo("2023-12-31T17:00:00.000Z")
  }

  @Test
  fun testBugHours2() {
    val startDate = ZonedDateTime.of(2023,12,31,16,1,47, 0, ZoneOffset.UTC).minusNanos(123)
    val endDate = ZonedDateTime.of(2024,1,1,14,39,26, TimeUnit.MILLISECONDS.toNanos(349L).toInt(), ZoneOffset.UTC)

    val startMillis = startDate.toDoubleMillis()
    assertThat(startMillis.formatUtc()).isEqualTo("2023-12-31T16:01:46.999Z")

    val ticks = DistanceHours(3).calculateTicks(startMillis, endDate.toDoubleMillis(), TimeZone.Berlin)
    assertThat(ticks[0].formatUtc()).isEqualTo("2023-12-31T14:00:00.000Z")
    assertThat(ticks[1].formatUtc()).isEqualTo("2023-12-31T17:00:00.000Z")
  }
}

