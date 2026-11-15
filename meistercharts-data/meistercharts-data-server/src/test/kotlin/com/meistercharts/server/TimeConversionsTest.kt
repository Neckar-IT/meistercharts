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
@file:Suppress("RETURN_VALUE_NOT_USED") // Test/Demo-scope: return values intentionally discarded

package com.meistercharts.server

import assertk.*
import assertk.assertions.*
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.DisableLogging
import it.neckar.open.time.millis
import it.neckar.open.time.millisToUtc
import it.neckar.open.time.toMillis
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

@DisableLogging
class TimeConversionsTest{
  @Test
  fun testStringConversion(): Unit {
    val year = 2001
    assertThat(year.toString()).equals("2001")
  }

  @Test
  fun testUTCConversion(): Unit {
    val incomingDouble = 1.5900732415E12
    val utcConversion = incomingDouble.formatUtc()
    assertThat(utcConversion).isEqualTo("2020-05-21T15:00:41.500Z")
    val year = utcConversion.slice(IntRange(0,3))
    val month = utcConversion.slice(IntRange(5,6))
    val day = utcConversion.slice(IntRange(8,9))
    val hour = utcConversion.slice(IntRange(11,12))
    val min = utcConversion.slice(IntRange(14,15))
    val millis = utcConversion.slice(IntRange(17,22))
    assertThat(year).isEqualTo("2020")
    assertThat(month).isEqualTo("05")
    assertThat(day).isEqualTo("21")
    assertThat(hour).isEqualTo("15")
    assertThat(min).isEqualTo("00")
    assertThat(millis).isEqualTo("41.500")
  }

  @Test
  fun testConvertMillisToDateTime(): Unit {
    val incomingDouble = 1.5900732415E12

    val dateTime = millisToUtc(incomingDouble)

    assertThat(dateTime.year).isEqualTo(2020)
    assertThat(dateTime.monthValue).isEqualTo(5)
    assertThat(dateTime.dayOfMonth).isEqualTo(21)
    assertThat(dateTime.hour).isEqualTo(15)
    assertThat(dateTime.minute).isEqualTo(0)
    assertThat(dateTime.second).isEqualTo(41)
    assertThat(dateTime.millis).isEqualTo(500)
  }

  @Test
  fun testConvertMillisToDateTimeRoundtrip(): Unit {
    val offset = OffsetDateTime.of(2020, 5, 21, 15, 0, 0, 0, ZoneOffset.UTC)
    val dateTime = millisToUtc(offset.toMillis()) // 1.5900732E12
    assertThat(dateTime.year).isEqualTo(2020)
    assertThat(dateTime.monthValue).isEqualTo(5)
    assertThat(dateTime.dayOfMonth).isEqualTo(21)
    assertThat(dateTime.hour).isEqualTo(15)
    assertThat(dateTime.minute).isEqualTo(0)
    assertThat(dateTime.second).isEqualTo(0)
    assertThat(dateTime.millis).isEqualTo(0)
  }
}


