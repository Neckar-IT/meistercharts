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
import it.neckar.open.formatting.dateTimeFormatIso8601
import it.neckar.open.formatting.dateFormat
import it.neckar.open.formatting.dateTimeFormatUTC
import it.neckar.open.formatting.dateTimeFormat
import it.neckar.open.formatting.dateTimeFormatShort
import it.neckar.open.formatting.dateTimeFormatShortWithMillis
import it.neckar.open.formatting.dateTimeFormatWithMillis
import it.neckar.open.formatting.formatUtc
import it.neckar.open.formatting.secondMillisFormat
import it.neckar.open.formatting.timeFormat
import it.neckar.open.formatting.timeFormatWithMillis
import it.neckar.open.formatting.yearMonthFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.Locale
import it.neckar.open.time.TimeZone
import org.junit.jupiter.api.Test

/**
 * The same tests as DateFormatJsTest
 */
class DateFormatJvmTest {
  val now: Double = 1615761971002.0

  val englishTokyo: I18nConfiguration = I18nConfiguration(
    Locale.US,
    Locale.US,
    TimeZone.Tokyo
  )

  val englishBerlin: I18nConfiguration = I18nConfiguration(
    Locale.US,
    Locale.US,
    TimeZone.Berlin
  )

  @Test
  fun dateFormat() {
    assertThat(now.formatUtc()).isEqualTo("2021-03-14T22:46:11.002")
    assertThat(dateFormat.format(now, I18nConfiguration.Germany)).isEqualTo("14.03.2021")
    assertThat(dateFormat.format(now, englishBerlin)).isEqualTo("Mar 14, 2021")
    assertThat(dateFormat.format(now, englishTokyo)).isEqualTo("Mar 15, 2021")
  }

  @Test
  fun dateTimeFormat() {
    assertThat(dateTimeFormat.format(now, I18nConfiguration.Germany)).isEqualTo("14.03.2021 23:46:11")
    assertThat(dateTimeFormat.format(now, englishBerlin)).isEqualTo("Mar 14, 2021 11:46:11 PM")
    assertThat(dateTimeFormat.format(now, englishTokyo)).isEqualTo("Mar 15, 2021 7:46:11 AM")
  }

  @Test
  fun dateTimeFormatShort() {
    assertThat(dateTimeFormatShort.format(now, I18nConfiguration.Germany)).isEqualTo("14.03.21 23:46")
    assertThat(dateTimeFormatShort.format(now, englishBerlin)).isEqualTo("3/14/21 11:46 PM")
    assertThat(dateTimeFormatShort.format(now, englishTokyo)).isEqualTo("3/15/21 7:46 AM")
  }

  @Test
  fun dateTimeFormatWithMillis() {
    assertThat(dateTimeFormatWithMillis.format(now, I18nConfiguration.Germany)).isEqualTo("14.03.2021 23:46:11.002")
    assertThat(dateTimeFormatWithMillis.format(now, englishBerlin)).isEqualTo("Mar 14, 2021 11:46:11.002 PM")
    assertThat(dateTimeFormatWithMillis.format(now, englishTokyo)).isEqualTo("Mar 15, 2021 7:46:11.002 AM")
  }

  @Test
  fun dateTimeFormatShortWithMillis() {
    assertThat(dateTimeFormatShortWithMillis.format(now, I18nConfiguration.Germany)).isEqualTo("14.03.21 23:46:11.002")
    assertThat(dateTimeFormatShortWithMillis.format(now, englishBerlin)).isEqualTo("3/14/21 11:46:11.002 PM")
    assertThat(dateTimeFormatShortWithMillis.format(now, englishTokyo)).isEqualTo("3/15/21 7:46:11.002 AM")
  }

  @Test
  fun dataFormatIso8601() {
    assertThat(dateTimeFormatIso8601.format(now, I18nConfiguration.Germany)).isEqualTo("2021-03-14T23:46:11.002+01:00[Europe/Berlin]")
    assertThat(dateTimeFormatIso8601.format(now, englishBerlin)).isEqualTo("2021-03-14T23:46:11.002+01:00[Europe/Berlin]")
    assertThat(dateTimeFormatIso8601.format(now, englishTokyo)).isEqualTo("2021-03-15T07:46:11.002+09:00[Asia/Tokyo]")
  }

  @Test
  fun dateFormatUTC() {
    assertThat(dateTimeFormatUTC.format(now, I18nConfiguration.Germany)).isEqualTo("2021-03-14T22:46:11.002")
    assertThat(dateTimeFormatUTC.format(now, englishBerlin)).isEqualTo("2021-03-14T22:46:11.002")
    assertThat(dateTimeFormatUTC.format(now, englishTokyo)).isEqualTo("2021-03-14T22:46:11.002")
  }

  @Test
  fun timeFormat() {
    assertThat(timeFormat.format(now, I18nConfiguration.Germany)).isEqualTo("23:46:11")
    assertThat(timeFormat.format(now, englishBerlin)).isEqualTo("11:46:11 PM")
    assertThat(timeFormat.format(now, englishTokyo)).isEqualTo("7:46:11 AM")
  }

  @Test
  fun timeFormatWithMillis() {
    assertThat(timeFormatWithMillis.format(now, I18nConfiguration.Germany)).isEqualTo("23:46:11.002")
    assertThat(timeFormatWithMillis.format(now, englishBerlin)).isEqualTo("11:46:11.002 PM")
    assertThat(timeFormatWithMillis.format(now, englishTokyo)).isEqualTo("7:46:11.002 AM")
  }

  @Test
  fun yearMonthFormat() {
    assertThat(yearMonthFormat.format(now, I18nConfiguration.Germany)).isEqualTo("März 2021")
    assertThat(yearMonthFormat.format(now, englishBerlin)).isEqualTo("March 2021")
    assertThat(yearMonthFormat.format(now, englishTokyo)).isEqualTo("March 2021")
  }

  @Test
  fun secondMilliSecondFormat() {
    assertThat(secondMillisFormat.format(now, I18nConfiguration.Germany)).isEqualTo("11,002")
    assertThat(secondMillisFormat.format(now, englishBerlin)).isEqualTo("11.002")
    assertThat(secondMillisFormat.format(now, englishTokyo)).isEqualTo("11.002")
  }
}
