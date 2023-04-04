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

import com.meistercharts.algorithms.LinearValueRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PaintableLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.StackedBarWithLabelPaintable
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.section
import it.neckar.open.provider.DefaultDoublesProvider
import com.meistercharts.style.Palette.chartColors
import it.neckar.open.kotlin.lang.enumEntries

/**
 */
class StackedBarWithLabelDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Stacked Bar with label"
  override val description: String = "Demo that  demontrates the stacked bar with label paintable"

  override val category: DemoCategory = DemoCategory.Paintables

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
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

        configurableDouble("width", paintable::width) {
          max = 200.0
        }

        configurableDouble("height", paintable::height) {
          max = 600.0
          value = paintable.height
        }

        configurableDouble("Value Range: Start", paintable.data.valueRange.start) {
          min = -100.0
          max = 100.0

          onChange {
            paintable.data.valueRange = (paintable.data.valueRange as LinearValueRange).withStart(it)
            markAsDirty()
          }
        }

        configurableDouble("Value Range: End", paintable.data.valueRange.end) {
          min = -100.0
          max = 100.0

          onChange {
            paintable.data.valueRange = paintable.data.valueRange.withEnd(it)
            markAsDirty()
          }
        }

        section("Value Label")

        configurableDouble("Gap Horizontal", paintable.stackedBarPaintable.style::valueLabelGapHorizontal) {
          min = 0.0
          max = 10.0
        }
        configurableDouble("Gap Vertical", paintable.stackedBarPaintable.style::valueLabelGapVertical) {
          min = 0.0
          max = 10.0
        }
        configurableBoolean("Visible", paintable.stackedBarPaintable.style::showValueLabels)
        configurableEnum("Anchor Direction", paintable.stackedBarPaintable.style::valueLabelAnchorDirection, enumEntries())
        configurableFont("Font", paintable.stackedBarPaintable.style::valueLabelFont)


        section("Background")

        configurableDouble("bg radius", paintable.stackedBarPaintable.style::backgroundRadius) {
          max = 10.0
        }

        configurableBoolean("Paint background", paintable.stackedBarPaintable.style::paintBackground)
        configurableColor("BG", paintable.stackedBarPaintable.style::backgroundColor)
      }
    }
  }
}
