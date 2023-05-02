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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.formatting.dateFormat
import it.neckar.open.formatting.dateTimeFormat
import it.neckar.open.formatting.dateTimeFormatIso8601
import it.neckar.open.formatting.dateTimeFormatWithMillis
import it.neckar.open.formatting.timeFormat
import it.neckar.open.formatting.timeFormatWithMillis
import it.neckar.open.formatting.yearMonthFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.Locale
import it.neckar.open.i18n.TextService
import it.neckar.open.time.TimeZone

/**
 */
class DateTimeFormatDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Date Time Format"

  //language=HTML
  override val description: String = "# How to format date and time"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    val timestamp = nowMillis()

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addText({ _: TextService, i18nConfiguration: I18nConfiguration ->
            val utcUsConfiguration = I18nConfiguration(
              textLocale = Locale.US,
              formatLocale = Locale.US,
              timeZone = TimeZone.UTC
            )

            val berlinDeConfiguration = I18nConfiguration(
              textLocale = Locale.Germany,
              formatLocale = Locale.Germany,
              timeZone = TimeZone.Berlin
            )

            val tokyoUsConfiguration = I18nConfiguration(
              textLocale = Locale.US,
              formatLocale = Locale.US,
              timeZone = TimeZone.Tokyo
            )

            val tokyoDeConfiguration = I18nConfiguration(
              textLocale = Locale.Germany,
              formatLocale = Locale.Germany,
              timeZone = TimeZone.Tokyo
            )

            listOf(
              "ISO8601: UTC, ${i18nConfiguration.formatLocale}: ${dateTimeFormatIso8601.format(timestamp, utcUsConfiguration)}",
              "ISO8601: Berlin, ${i18nConfiguration.formatLocale}: ${dateTimeFormatIso8601.format(timestamp, berlinDeConfiguration)}",
              "ISO8601: Tokyo, ${i18nConfiguration.formatLocale}: ${dateTimeFormatIso8601.format(timestamp, tokyoUsConfiguration)}",
              "ISO8601: Tokyo, de-DE: ${dateTimeFormatIso8601.format(timestamp, tokyoDeConfiguration)}",
              "",
              "DateTimeMillis: UTC, ${i18nConfiguration.formatLocale}: ${dateTimeFormatWithMillis.format(timestamp, utcUsConfiguration)}",
              "DateTimeMillis: Berlin, ${i18nConfiguration.formatLocale}: ${dateTimeFormatWithMillis.format(timestamp, berlinDeConfiguration)}",
              "DateTimeMillis: Tokyo, ${i18nConfiguration.formatLocale}: ${dateTimeFormatWithMillis.format(timestamp, tokyoUsConfiguration)}",
              "DateTimeMillis: Tokyo, de-DE: " + dateTimeFormatWithMillis.format(timestamp, tokyoDeConfiguration),
              "",
              "DateTime: UTC, ${i18nConfiguration.formatLocale}: ${dateTimeFormat.format(timestamp, utcUsConfiguration)}",
              "DateTime: Berlin, ${i18nConfiguration.formatLocale}: ${dateTimeFormat.format(timestamp, berlinDeConfiguration)}",
              "DateTime: Tokyo, ${i18nConfiguration.formatLocale}: ${dateTimeFormat.format(timestamp, tokyoUsConfiguration)}",
              "DateTime: Tokyo, de-DE: " + dateTimeFormat.format(timestamp, tokyoDeConfiguration),
              "",
              "Date: UTC, ${i18nConfiguration.formatLocale}: ${dateFormat.format(timestamp, utcUsConfiguration)}",
              "Date: Berlin, ${i18nConfiguration.formatLocale}: ${dateFormat.format(timestamp, berlinDeConfiguration)}",
              "Date: Tokyo, ${i18nConfiguration.formatLocale}: ${dateFormat.format(timestamp, tokyoUsConfiguration)}",
              "Date: Tokyo, de-DE: ${dateFormat.format(timestamp, tokyoDeConfiguration)}",
              "",
              "TimeMillis: UTC, ${i18nConfiguration.formatLocale}: ${timeFormatWithMillis.format(timestamp, utcUsConfiguration)}",
              "TimeMillis: Berlin, ${i18nConfiguration.formatLocale}: ${timeFormatWithMillis.format(timestamp, berlinDeConfiguration)}",
              "TimeMillis: Tokyo, ${i18nConfiguration.formatLocale}: ${timeFormatWithMillis.format(timestamp, tokyoUsConfiguration)}",
              "TimeMillis: Tokyo, de-DE: ${timeFormatWithMillis.format(timestamp, tokyoDeConfiguration)}",
              "",
              "Time: UTC, ${i18nConfiguration.formatLocale}: ${timeFormat.format(timestamp, utcUsConfiguration)}",
              "Time: Berlin, ${i18nConfiguration.formatLocale}: ${timeFormat.format(timestamp, berlinDeConfiguration)}",
              "Time: Tokyo, ${i18nConfiguration.formatLocale}: ${timeFormat.format(timestamp, tokyoUsConfiguration)}",
              "Time: Tokyo, de-DE: ${timeFormat.format(timestamp, tokyoDeConfiguration)}",
              "",
              "YearMonth: UTC, ${i18nConfiguration.formatLocale}: ${yearMonthFormat.format(timestamp, utcUsConfiguration)}",
              "YearMonth: Berlin, ${i18nConfiguration.formatLocale}: ${yearMonthFormat.format(timestamp, berlinDeConfiguration)}",
              "YearMonth: Tokyo, ${i18nConfiguration.formatLocale}: ${yearMonthFormat.format(timestamp, tokyoUsConfiguration)}",
              "YearMonth: Tokyo, de-DE: ${yearMonthFormat.format(timestamp, tokyoDeConfiguration)}"
            )
          }) {
            anchorDirection = Direction.TopLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            textColor = Color.black
            font = FontDescriptorFragment(15.0)
          }
        }
      }
    }
  }
}
