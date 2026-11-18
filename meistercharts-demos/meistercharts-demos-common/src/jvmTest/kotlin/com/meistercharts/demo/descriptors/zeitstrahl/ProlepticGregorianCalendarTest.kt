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
package com.meistercharts.demo.descriptors.zeitstrahl

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ProlepticGregorianCalendarTest {
  @Test
  fun `the storming of the Bastille lies at the seconds of the concept`() {
    assertThat(ProlepticGregorianCalendar.secondsOf(CivilDate(1789, 7, 14))).isEqualTo(-5_694_969_600.0)
    assertThat(ProlepticGregorianCalendar.civilDate(-5_694_969_600.0)).isEqualTo(CivilDate(1789, 7, 14))
  }

  @Test
  fun `the epoch is day 0`() {
    assertThat(ProlepticGregorianCalendar.civilDateOfDay(0)).isEqualTo(CivilDate(1970, 1, 1))
    assertThat(ProlepticGregorianCalendar.civilDateOfDay(-1)).isEqualTo(CivilDate(1969, 12, 31))
  }

  @ParameterizedTest
  @ValueSource(longs = [-13_787_000_000, -10_000, -44, 0, 1582, 1789, 2026])
  fun `converts the 28th of February, the 1st of March and the 31st of December back and forth`(year: Long) {
    listOf(CivilDate(year, 2, 28), CivilDate(year, 3, 1), CivilDate(year, 12, 31)).forEach { date ->
      assertThat(ProlepticGregorianCalendar.civilDateOfDay(ProlepticGregorianCalendar.dayOf(date))).isEqualTo(date)
    }
  }

  @ParameterizedTest
  @ValueSource(longs = [-13_787_000_000, -10_000, -44, 0, 2024])
  fun `a leap year has the 29th of February`(year: Long) {
    assertThat(ProlepticGregorianCalendar.daysInMonth(year, 2)).isEqualTo(29)
    assertThat(ProlepticGregorianCalendar.civilDateOfDay(ProlepticGregorianCalendar.dayOf(CivilDate(year, 2, 28)) + 1)).isEqualTo(CivilDate(year, 2, 29))
  }

  @ParameterizedTest
  @ValueSource(longs = [-43, 1582, 1700, 1789, 1900, 2026])
  fun `a common year ends February on the 28th`(year: Long) {
    assertThat(ProlepticGregorianCalendar.daysInMonth(year, 2)).isEqualTo(28)
  }
}
