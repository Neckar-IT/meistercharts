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
package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.axis.time.addTimeAxis
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.algorithms.layers.text.addTexts
import com.meistercharts.canvas.textService
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle
import com.meistercharts.time.TimeRange
import it.neckar.open.i18n.CurrentI18nConfiguration
import it.neckar.open.i18n.Locale
import it.neckar.open.i18n.SystemI18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.resolve
import it.neckar.open.i18n.resolve.MapBasedTextResolver
import it.neckar.open.kotlin.lang.asProvider

/**
 */

/**
 * Demos that visualizes the functionality of the FPS layer
 */
class I18nTranslationDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("a82f7462-0c6f-46b8-b33f-e81e862513c4")
  override val name: String = "I18n"
  override val description: String = "How to translate text"
  override val category: DemoCategory = DemoCategory.Text
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val textKey = TextKey("my.message", "Default Text 1")
        val timeAxisKey = TextKey("time.axis.key", "Default Text 2")

        configureAsTimeChart()

        configure {
          chartSupport.textService.addTextResolverAtFirst(MapBasedTextResolver().also {
            it.setText(Locale.US, textKey, "Message - US")
            it.setText(Locale.Germany, textKey, "Message - DE")

            it.setText(Locale.US, timeAxisKey, "Time")
            it.setText(Locale.Germany, timeAxisKey, "Zeit")
          })

          layers.addClearBackground()
          val boxStyle = BoxStyle(Color.gray, Color.darkgrey, padding = Insets.of(5.0))
          layers.addText(textKey) {
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }

          layers.addTexts { textService, i18nConfiguration ->
            listOf(
              "Parameter I18n Configuration: ${i18nConfiguration.textLocale}",
              "System I18n Configuration: ${SystemI18nConfiguration}",
              "Default I18n Configuration: $CurrentI18nConfiguration",
            )
          }.also {
            it.configuration.topLeft()
            it.configuration.font = FontDescriptorFragment.DefaultSize.asProvider()
          }

          val contentAreaTimeRange = TimeRange.oneMinuteUntilNow()

          layers.addTimeAxis(contentAreaTimeRange) {
            titleProvider = { textService, i18nConfiguration -> timeAxisKey.resolve(textService, i18nConfiguration) }
          }
        }
      }
    }
  }
}
