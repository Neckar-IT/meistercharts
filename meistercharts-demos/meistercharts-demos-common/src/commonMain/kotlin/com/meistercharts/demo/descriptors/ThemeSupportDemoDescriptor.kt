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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTexts
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.theme
import com.meistercharts.canvas.themeSupport
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableList
import com.meistercharts.design.CurrentTheme
import com.meistercharts.design.DebugTheme
import com.meistercharts.design.NeckarITDesign
import com.meistercharts.design.Theme
import com.meistercharts.design.ThemeContext
import com.meistercharts.font.FontDescriptorFragment
import it.neckar.geometry.Distance
import it.neckar.geometry.HorizontalAlignment
import it.neckar.open.kotlin.lang.asProvider

/**
 *
 */
class ThemeSupportDemoDescriptor : MeisterchartsDemoDescriptor<Any?> {
  override val uuid: Uuid = Uuid.parse("b5cccd43-fc15-4f11-8847-da654f019925")
  override val name: String = "Theme Support"

  override val category: DemoCategory = DemoCategory.Support
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Any?>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          chartSupport.themeSupport.selectedTheme = DebugTheme

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val chartSupport = paintingContext.chartSupport
              val gc = paintingContext.gc

              val design = chartSupport.theme

              gc.fill(design.backgroundColorActive)
              gc.fillRect(10.0, 10.0, 100.0, 100.0)

              gc.fill(design.secondaryBackgroundColor)
              gc.fillRect(10.0, 110.0, 100.0, 100.0)
            }
          })

          layers.addTexts { _, _ ->
            listOf(
              "Default Theme: ${ThemeContext.defaultValue.id}",
              "Current Theme: ${CurrentTheme.id}",
              "chartSupport.theme: ${chartSupport.theme.id}",
            )
          }.also {
            it.configuration.font = FontDescriptorFragment.S.asProvider()
            it.configuration.horizontalAlignment = HorizontalAlignment.Left
            it.configuration.topLeft(Distance.of(150.0, 40.0))
          }

          layers.addTexts { _, _ ->
            listOf(
              "Example test with theme button font!",
              Theme.buttonFont.resolve().toString(),
            )
          }.also {
            it.configuration.font = Theme.buttonFont.provider()
          }

          configurableList(
            propertyName = "Theme",
            initial = chartSupport.theme,
            possibleValues = listOf(
              CurrentTheme,
              NeckarITDesign,
              ThemeContext.defaultValue,
              DebugTheme,
            )
          ) {
            converter { it.id }

            onChange {
              chartSupport.themeSupport.selectedTheme = it
              markAsDirty(DirtyReason.UserInteraction)
            }
          }
        }
      }
    }
  }
}
