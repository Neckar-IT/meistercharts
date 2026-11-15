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

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.ValueRange
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PaintableLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.StackedBarWithLabelPaintable
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.section
import it.neckar.open.provider.DefaultDoublesProvider
import com.meistercharts.style.Palette.chartColors
import it.neckar.geometry.Direction

/**
 */
class StackedBarWithLabelDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("b713f765-9e51-4777-bcd7-0a2062e965d6")
  override val name: String = "Stacked Bar with label"
  override val description: String = "Shows the stacked bar paintable with a label."

  override val variabilityType: VariabilityType = VariabilityType.Stable
  override val quality: DemoQuality = DemoQuality.High

  override val category: DemoCategory = DemoCategory.Paintables

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        val valuesProvider = DefaultDoublesProvider(listOf(1.0, 7.0, 20.0, 12.0))
        val valueRange = ValueRange.linear(0.0, 30.0)

        val paintable = StackedBarWithLabelPaintable("The Label value", valuesProvider, valueRange, chartColors, 15.0, 200.0)

        configure {
          val layer = PaintableLayer(
            PaintableLayer.PaintableLayoutMode.Paintable
          ) { _ ->
            paintable
          }

          layers.addClearBackground()
          layers.addLayer(WindowDebugLayer(LayerType.Content))
          layers.addLayer(layer)
        }

        configurable("width", paintable::width) {
          max = 200.0
        }

        configurable("height", paintable::height) {
          max = 600.0
          value = paintable.height
        }

        configurableDouble("Value Range: Start", paintable.configuration.valueRange.start) {
          min = -100.0
          max = 100.0

          onChange {
            paintable.configuration.valueRange = (paintable.configuration.valueRange as LinearValueRange).withStart(it)
            markAsDirty()
          }
        }

        configurableDouble("Value Range: End", paintable.configuration.valueRange.end) {
          min = -100.0
          max = 100.0

          onChange {
            paintable.configuration.valueRange = paintable.configuration.valueRange.withEnd(it)
            markAsDirty()
          }
        }

        section("Value Label")

        configurable("Gap Horizontal", paintable.stackedBarPaintable.style::valueLabelGapHorizontal) {
          min = 0.0
          max = 10.0
        }
        configurable("Gap Vertical", paintable.stackedBarPaintable.style::valueLabelGapVertical) {
          min = 0.0
          max = 10.0
        }
        configurable("Visible", paintable.stackedBarPaintable.style::showValueLabels)
        configurable("Anchor Direction", paintable.stackedBarPaintable.style::valueLabelAnchorDirection, kotlin.enums.enumEntries<Direction>().toList())
        configurable("Font", paintable.stackedBarPaintable.style::valueLabelFont)


        section("Background")

        configurable("bg radius", paintable.stackedBarPaintable.style::backgroundRadius) {
          max = 10.0
        }

        configurable("Paint background", paintable.stackedBarPaintable.style::paintBackground)
        configurableColorProvider("BG", paintable.stackedBarPaintable.style::backgroundColor)
      }
    }
  }
}
