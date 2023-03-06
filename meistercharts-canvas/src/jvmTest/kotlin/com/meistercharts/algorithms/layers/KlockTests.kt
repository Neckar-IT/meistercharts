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
package com.meistercharts.algorithms.layers

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.meistercharts.algorithms.axis.local2DateTimeTz
import com.meistercharts.algorithms.axis.utc2DateTimeTz
import com.meistercharts.algorithms.time.timeZoneOffsetProvider
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.TimeZone
import com.soywiz.klock.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


/**
 *
 */
class KlockTests {
  @Test
  fun testUtc() {
    val zoneId = ZoneId.of("Europe/Berlin")

    val localDateTime = LocalDateTime.of(2021, java.time.Month.MARCH, 28, 0, 0)

    assertThat(ZonedDateTime.of(localDateTime, zoneId).format(DateTimeFormatter.ISO_DATE_TIME)).isEqualTo("2021-03-28T00:00:00+01:00[Europe/Berlin]")

    //+ 1 day on local date time
    assertThat(ZonedDateTime.of(localDateTime.plusDays(1), zoneId).format(DateTimeFormatter.ISO_DATE_TIME)).isEqualTo("2021-03-29T00:00:00+02:00[Europe/Berlin]")

    //
    assertThat(ZonedDateTime.of(localDateTime, zoneId).plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME)).isEqualTo("2021-03-29T00:00:00+02:00[Europe/Berlin]")
    //*NOT* the same - sommer saving time
    assertThat(ZonedDateTime.of(localDateTime, zoneId).plusHours(24).format(DateTimeFormatter.ISO_DATE_TIME)).isEqualTo("2021-03-29T01:00:00+02:00[Europe/Berlin]")


    val dateTime = DateTime(2021, Month.March, 28, 0, 0, 0, 0)

    assertThat(dateTime.yearInt).isEqualTo(2021)
    assertThat(dateTime.dayOfMonth).isEqualTo(28)
    assertThat(dateTime.hours).isEqualTo(0)

    assertThat(dateTime.unixMillis.formatUtc()).isEqualTo("2021-03-28T00:00:00.000")

    val oneDay = DateTimeSpan(0, 0, 0, 1, 0, 0, 0, 0.0)
    val twentyFourHours = DateTimeSpan(0, 0, 0, 0, 24, 0, 0, 0.0)
    dateTime.plus(oneDay).let {
      assertThat(it.unixMillis.formatUtc()).isEqualTo("2021-03-29T00:00:00.000")
    }
    dateTime.plus(twentyFourHours).let {
      assertThat(it.unixMillis.formatUtc()).isEqualTo("2021-03-29T00:00:00.000")
    }
  }

  @Test
  fun testConversions() {
    val oneDay = DateTimeSpan(0, 0, 0, 1, 0, 0, 0, 0.0)
    val twentyFourHours = DateTimeSpan(0, 0, 0, 0, 24, 0, 0, 0.0)

    val dateTime = DateTime(2021, Month.March, 28, 0, 0, 0, 0)
    assertThat(dateTime.unixMillis.formatUtc()).isEqualTo("2021-03-28T00:00:00.000")

    dateTime.utc2DateTimeTz(TimeZone.Berlin).let {
      assertThat(it.format(DateFormat.FORMAT2)).isEqualTo("2021-03-28T01:00:00.000Z")
      assertThat(it.yearInt).isEqualTo(2021)
      assertThat(it.month1).isEqualTo(3)
      assertThat(it.dayOfMonth).isEqualTo(28)
      assertThat(it.hours).isEqualTo(1)
      assertThat(it.minutes).isEqualTo(0)
      assertThat(it.offset.timeZone).isEqualTo("GMT+0100")
      assertThat(it.utc.unixMillis.formatUtc()).isEqualTo("2021-03-28T00:00:00.000")

      it.plus(oneDay).let {
        assertThat(it.format(DateFormat.FORMAT2)).isEqualTo("2021-03-29T01:00:00.000Z")
      }
      it.plus(twentyFourHours).let {
        assertThat(it.format(DateFormat.FORMAT2)).isEqualTo("2021-03-29T01:00:00.000Z")
      }
    }

    dateTime.local2DateTimeTz(TimeZone.Berlin).let {
      assertThat(it.format(DateFormat.FORMAT2)).isEqualTo("2021-03-28T00:00:00.000Z")
      assertThat(it.yearInt).isEqualTo(2021)
      assertThat(it.month1).isEqualTo(3)
      assertThat(it.dayOfMonth).isEqualTo(28)
      assertThat(it.hours).isEqualTo(0)
      assertThat(it.minutes).isEqualTo(0)
      assertThat(it.offset.timeZone).isEqualTo("GMT+0100")
      assertThat(it.utc.unixMillis.formatUtc()).isEqualTo("2021-03-27T23:00:00.000")

      it.plus(oneDay).let {
        assertThat(it.format(DateFormat.FORMAT2)).isEqualTo("2021-03-29T00:00:00.000Z")
      }
      it.plus(twentyFourHours).let {
        assertThat(it.format(DateFormat.FORMAT2)).isEqualTo("2021-03-29T00:00:00.000Z")
      }
    }
  }

  @Test
  fun testIt() {
    val dateTime = DateTime(2020, Month.February, 7).also {
      assertThat(it.format(DateFormat.DEFAULT_FORMAT)).isEqualTo("Fri, 07 Feb 2020 00:00:00 UTC")
      assertThat(it.yearInt).isEqualTo(2020)
      assertThat(it.month).isEqualTo(Month.February)
      assertThat(it.dayOfMonth).isEqualTo(7)
      assertThat(it.hours).isEqualTo(0)
      assertThat(it.minutes).isEqualTo(0)

      timeZoneOffsetProvider.timeZoneOffset(it.unixMillisDouble, TimeZone.Berlin).let {
        assertThat(it.milliseconds.hours).isEqualTo(1.0)
      }
    }

    dateTime.local2DateTimeTz(TimeZone.Berlin).let {
      assertThat(it.year.year).isEqualTo(2020)
      assertThat(it.month).isEqualTo(Month.February)
      assertThat(it.dayOfMonth).isEqualTo(7)
      assertThat(it.hours).isEqualTo(0)
      assertThat(it.minutes).isEqualTo(0)

      assertThat(it.local.hours).isEqualTo(0)
    }

    dateTime.local2DateTimeTz(TimeZone.Tokyo).let {
      assertThat(it.year.year).isEqualTo(2020)
      assertThat(it.month).isEqualTo(Month.February)
      assertThat(it.dayOfMonth).isEqualTo(7)
      assertThat(it.hours).isEqualTo(0)
      assertThat(it.minutes).isEqualTo(0)
    }
  }

  @Test
  fun testDateTimePlusMonths() {
    val span = DateTimeSpan(0, 3)

    val dateTime = DateTime(2020, Month.January, 1).also {
      assertThat(it.yearInt).isEqualTo(2020)
      assertThat(it.month).isEqualTo(Month.January)
      assertThat(it.dayOfMonth).isEqualTo(1)
      assertThat(it.hours).isEqualTo(0)
      assertThat(it.minutes).isEqualTo(0)
    }

    dateTime.plus(span).let {
      assertThat(it.yearInt).isEqualTo(2020)
      assertThat(it.month).isEqualTo(Month.April)
      assertThat(it.dayOfMonth).isEqualTo(1)
      assertThat(it.hours).isEqualTo(0)
      assertThat(it.minutes).isEqualTo(0)
    }
  }

  @Test
  fun testDateTimeTzPlusMonths() {
    val span = DateTimeSpan(0, 3)

    val dateTime = DateTime(2020, Month.January, 1).utc2DateTimeTz(TimeZone.Tokyo).also {
      assertThat(it.yearInt).isEqualTo(2020)
      assertThat(it.month).isEqualTo(Month.January)
      assertThat(it.dayOfMonth).isEqualTo(1)
      assertThat(it.hours).isEqualTo(9)
      assertThat(it.minutes).isEqualTo(0)
    }

    dateTime.plus(span).let {
      assertThat(it.yearInt).isEqualTo(2020)
      assertThat(it.month).isEqualTo(Month.April)
      assertThat(it.dayOfMonth).isEqualTo(1)
      assertThat(it.hours).isEqualTo(9)
      assertThat(it.minutes).isEqualTo(0)

      assertThat(it.utc.hours).isEqualTo(0)
    }
  }
}
