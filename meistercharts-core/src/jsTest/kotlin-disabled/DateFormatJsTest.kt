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
import assertk.assertThat
import assertk.assertions.isEqualTo
import it.neckar.open.formatting.*
import it.neckar.open.i18n.Locale
import it.neckar.open.time.TimeZone
import kotlin.js.Date
import kotlin.test.Ignore
import kotlin.test.Test

/**
 *
 */
class DateFormatJsTest {
  val now: Double = 1615761971002.0

  @Test
  fun dateFormat() {
    assertThat(now.formatUtc()).isEqualTo("2021-03-14T22:46:11.002Z")
    assertThat(dateFormat.format(now, TimeZone.Berlin, Locale.Germany)).isEqualTo("14.3.2021")
    assertThat(dateFormat.format(now, TimeZone.Berlin, Locale.US)).isEqualTo("3/14/2021")
    assertThat(dateFormat.format(now, TimeZone.Tokyo, Locale.US)).isEqualTo("3/15/2021")
  }

  @Test
  fun dateTimeFormat() {
    assertThat(dateTimeFormat.format(now, TimeZone.Berlin, Locale.Germany)).isEqualTo("14.3.2021, 23:46:11")
    assertThat(dateTimeFormat.format(now, TimeZone.Berlin, Locale.US)).isEqualTo("3/14/2021, 11:46:11 PM")
    assertThat(dateTimeFormat.format(now, TimeZone.Tokyo, Locale.US)).isEqualTo("3/15/2021, 7:46:11 AM")
  }

  @Test
  fun dateTimeFormatShort() {
    assertThat(dateTimeFormatShort.format(now, TimeZone.Berlin, Locale.Germany)).isEqualTo("14.03.21, 23:46")
    assertThat(dateTimeFormatShort.format(now, TimeZone.Berlin, Locale.US)).isEqualTo("3/14/21, 11:46 PM")
    assertThat(dateTimeFormatShort.format(now, TimeZone.Tokyo, Locale.US)).isEqualTo("3/15/21, 7:46 AM")
  }


  @Test
  fun dateTimeFormatWithMillis() {
    assertThat(dateTimeFormatWithMillis.format(now, TimeZone.Berlin, Locale.Germany)).isEqualTo("14.3.2021, 23:46:11.002")
    assertThat(dateTimeFormatWithMillis.format(now, TimeZone.Berlin, Locale.US)).isEqualTo("3/14/2021, 11:46:11.002 PM")
    assertThat(dateTimeFormatWithMillis.format(now, TimeZone.Tokyo, Locale.US)).isEqualTo("3/15/2021, 7:46:11.002 AM")
  }

  @Test
  fun dateTimeFormatShortWithMillis() {
    assertThat(dateTimeFormatShortWithMillis.format(now, TimeZone.Berlin, Locale.Germany)).isEqualTo("14.3.2021, 23:46:11.002")
    assertThat(dateTimeFormatShortWithMillis.format(now, TimeZone.Berlin, Locale.US)).isEqualTo("3/14/2021, 11:46:11.002 PM")
    assertThat(dateTimeFormatShortWithMillis.format(now, TimeZone.Tokyo, Locale.US)).isEqualTo("3/15/2021, 7:46:11.002 AM")
  }

  @Test
  fun dataFormatIso8601() {
    assertThat(dataFormatIso8601.format(now, TimeZone.Berlin, Locale.Germany)).isEqualTo("2021-03-14T22:46:11.002Z")
    assertThat(dataFormatIso8601.format(now, TimeZone.Berlin, Locale.US)).isEqualTo("2021-03-14T22:46:11.002Z")
    assertThat(dataFormatIso8601.format(now, TimeZone.Tokyo, Locale.US)).isEqualTo("2021-03-14T22:46:11.002Z")
  }

  @Test
  fun dateFormatUTC() {
    assertThat(dateFormatUTC.format(now, TimeZone.Berlin, Locale.Germany)).isEqualTo("2021-03-14T22:46:11.002Z")
    assertThat(dateFormatUTC.format(now, TimeZone.Berlin, Locale.US)).isEqualTo("2021-03-14T22:46:11.002Z")
    assertThat(dateFormatUTC.format(now, TimeZone.Tokyo, Locale.US)).isEqualTo("2021-03-14T22:46:11.002Z")
  }

  @Test
  fun timeFormat() {
    assertThat(timeFormat.format(now, TimeZone.Berlin, Locale.Germany)).isEqualTo("23:46:11")
    assertThat(timeFormat.format(now, TimeZone.Berlin, Locale.US)).isEqualTo("11:46:11 PM")
    assertThat(timeFormat.format(now, TimeZone.Tokyo, Locale.US)).isEqualTo("7:46:11 AM")
  }

  @Test
  fun timeFormatWithMillis() {
    assertThat(timeFormatWithMillis.format(now, TimeZone.Berlin, Locale.Germany)).isEqualTo("23:46:11.002")
    assertThat(timeFormatWithMillis.format(now, TimeZone.Berlin, Locale.US)).isEqualTo("11:46:11.002 PM")
    assertThat(timeFormatWithMillis.format(now, TimeZone.Tokyo, Locale.US)).isEqualTo("7:46:11.002 AM")
  }

  @Test
  fun yearMonthFormat() {
    assertThat(yearMonthFormat.format(now, TimeZone.Berlin, Locale.Germany)).isEqualTo("März 2021")
    assertThat(yearMonthFormat.format(now, TimeZone.Berlin, Locale.US)).isEqualTo("March 2021")
    assertThat(yearMonthFormat.format(now, TimeZone.Tokyo, Locale.US)).isEqualTo("March 2021")
  }

  @Test
  fun secondMilliSecondFormat() {
    assertThat(secondMillisFormat.format(now, TimeZone.Berlin, Locale.Germany)).isEqualTo("11,002")
    assertThat(secondMillisFormat.format(now, TimeZone.Berlin, Locale.US)).isEqualTo("11.002")
    assertThat(secondMillisFormat.format(now, TimeZone.Tokyo, Locale.US)).isEqualTo("11.002")
  }

  @Ignore
  @Test
  fun testSecondsMillis() {
    val date = Date(now)

    date.toLocaleString("default", dateLocaleOptions {
      timeZone = TimeZone.Tokyo.zoneId

      second = "numeric"
      //Attention!! Does *NOT* work with Safari atm
      asDynamic().fractionalSecondDigits = "3"

    }).let {
      assertThat(it).isEqualTo("11.002")
    }
  }
}
