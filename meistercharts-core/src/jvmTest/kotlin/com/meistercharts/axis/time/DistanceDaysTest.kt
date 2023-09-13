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
package com.meistercharts.axis.time

import com.meistercharts.axis.time.DistanceDays.TicksPerMonth
import it.neckar.datetime.minimal.DayOfMonth
import it.neckar.datetime.minimal.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DistanceDaysTest {
  @Test
  fun `sameOrBelow with EveryDay should return same day`() {
    assertThat(TicksPerMonth.EveryDay.sameOrBelow(DayOfMonth(7))).isEqualTo(DayOfMonth(7))
  }

  @Test
  fun `sameOrBelow with Every5Days should return same or below day`() {
    assertThat(TicksPerMonth.Every5Days.sameOrBelow(DayOfMonth(7))).isEqualTo(DayOfMonth(5))
    assertThat(TicksPerMonth.Every5Days.sameOrBelow(DayOfMonth(5))).isEqualTo(DayOfMonth(5))
    assertThat(TicksPerMonth.Every5Days.sameOrBelow(DayOfMonth(3))).isEqualTo(DayOfMonth(1))
  }

  @Test
  fun `sameOrBelow with Every10Days should return same or below day`() {
    assertThat(TicksPerMonth.Every10Days.sameOrBelow(DayOfMonth(12))).isEqualTo(DayOfMonth(10))
  }

  @Test
  fun `sameOrBelow with Every15Days should return same or below day`() {
    assertThat(TicksPerMonth.Every15Days.sameOrBelow(DayOfMonth(18))).isEqualTo(DayOfMonth(15))
  }

  @Test
  fun `calculateNext with EveryDay should return next day`() {
    assertThat(TicksPerMonth.EveryDay.calculateNext(LocalDate(2023, 9, 6))).isEqualTo(LocalDate(2023, 9, 7))
  }

  @Test
  fun `calculateNext with Every5Days should return next applicable day`() {
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 9, 6))).isEqualTo(LocalDate(2023, 9, 10))
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 9, 7))).isEqualTo(LocalDate(2023, 9, 10))
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 9, 10))).isEqualTo(LocalDate(2023, 9, 15))
  }

  @Test
  fun `calculateNext with Every10Days should return next applicable day`() {
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 9, 1))).isEqualTo(LocalDate(2023, 9, 10))
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 9, 2))).isEqualTo(LocalDate(2023, 9, 10))
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 9, 6))).isEqualTo(LocalDate(2023, 9, 10))
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 9, 9))).isEqualTo(LocalDate(2023, 9, 10))
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 9, 11))).isEqualTo(LocalDate(2023, 9, 20))
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 9, 21))).isEqualTo(LocalDate(2023, 10, 1))
  }

  @Test
  fun `calculateNext with Every15Days should return next applicable day`() {
    assertThat(TicksPerMonth.Every15Days.calculateNext(LocalDate(2023, 9, 6))).isEqualTo(LocalDate(2023, 9, 15))
  }

  @Test
  fun `calculateNext with Every5Days should return first of next month if no more days in current month`() {
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 9, 26))).isEqualTo(LocalDate(2023, 10, 1))
  }

  @Test
  fun `calculateNext with EveryDay should handle all scenarios`() {
    assertThat(TicksPerMonth.EveryDay.calculateNext(LocalDate(2023, 9, 1))).isEqualTo(LocalDate(2023, 9, 2))
    assertThat(TicksPerMonth.EveryDay.calculateNext(LocalDate(2023, 9, 5))).isEqualTo(LocalDate(2023, 9, 6))
    assertThat(TicksPerMonth.EveryDay.calculateNext(LocalDate(2023, 9, 28))).isEqualTo(LocalDate(2023, 9, 29))
    assertThat(TicksPerMonth.EveryDay.calculateNext(LocalDate(2023, 9, 30))).isEqualTo(LocalDate(2023, 10, 1))
    assertThat(TicksPerMonth.EveryDay.calculateNext(LocalDate(2023, 12, 31))).isEqualTo(LocalDate(2024, 1, 1))
    assertThat(TicksPerMonth.EveryDay.calculateNext(LocalDate(2024, 2, 28))).isEqualTo(LocalDate(2024, 2, 29))
    assertThat(TicksPerMonth.EveryDay.calculateNext(LocalDate(2023, 2, 28))).isEqualTo(LocalDate(2023, 3, 1))
  }


  @Test
  fun `calculateNext with Every5Days should handle all scenarios`() {
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 9, 1))).isEqualTo(LocalDate(2023, 9, 5))
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 9, 5))).isEqualTo(LocalDate(2023, 9, 10))
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 9, 9))).isEqualTo(LocalDate(2023, 9, 10))
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 9, 10))).isEqualTo(LocalDate(2023, 9, 15))
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 9, 11))).isEqualTo(LocalDate(2023, 9, 15))
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 9, 28))).isEqualTo(LocalDate(2023, 10, 1))
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 12, 31))).isEqualTo(LocalDate(2024, 1, 1))
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2024, 2, 25))).isEqualTo(LocalDate(2024, 3, 1))
    assertThat(TicksPerMonth.Every5Days.calculateNext(LocalDate(2023, 2, 25))).isEqualTo(LocalDate(2023, 3, 1))
  }

  @Test
  fun `calculateNext with Every10Days should handle all scenarios`() {
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 9, 1))).isEqualTo(LocalDate(2023, 9, 10))
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 9, 5))).isEqualTo(LocalDate(2023, 9, 10))
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 9, 28))).isEqualTo(LocalDate(2023, 10, 1))
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 12, 11))).isEqualTo(LocalDate(2023, 12, 20))
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 12, 31))).isEqualTo(LocalDate(2024, 1, 1))
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2024, 2, 11))).isEqualTo(LocalDate(2024, 2, 20))
    assertThat(TicksPerMonth.Every10Days.calculateNext(LocalDate(2023, 2, 11))).isEqualTo(LocalDate(2023, 2, 20))
  }

  @Test
  fun `calculateNext with Every15Days should handle all scenarios`() {
    assertThat(TicksPerMonth.Every15Days.calculateNext(LocalDate(2023, 9, 1))).isEqualTo(LocalDate(2023, 9, 15))
    assertThat(TicksPerMonth.Every15Days.calculateNext(LocalDate(2023, 9, 5))).isEqualTo(LocalDate(2023, 9, 15))
    assertThat(TicksPerMonth.Every15Days.calculateNext(LocalDate(2023, 9, 16))).isEqualTo(LocalDate(2023, 10, 1))
    assertThat(TicksPerMonth.Every15Days.calculateNext(LocalDate(2023, 12, 2))).isEqualTo(LocalDate(2023, 12, 15))
    assertThat(TicksPerMonth.Every15Days.calculateNext(LocalDate(2023, 12, 31))).isEqualTo(LocalDate(2024, 1, 1))
    assertThat(TicksPerMonth.Every15Days.calculateNext(LocalDate(2024, 2, 2))).isEqualTo(LocalDate(2024, 2, 15))
    assertThat(TicksPerMonth.Every15Days.calculateNext(LocalDate(2023, 2, 2))).isEqualTo(LocalDate(2023, 2, 15))
  }
}
