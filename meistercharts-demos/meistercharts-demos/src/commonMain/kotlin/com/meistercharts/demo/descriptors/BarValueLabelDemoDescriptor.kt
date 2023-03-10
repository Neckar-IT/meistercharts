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

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.SeriesIndex
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableValueRange
import com.meistercharts.demo.section
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

class BarValueLabelDemoDescriptor : ChartingDemoDescriptor<BarValueLabelDemoDescriptor.Config> {

  override val name: String = "Bar chart with value labels"

  //language=HTML
  override val description: String = """
    <h2>Bar chart with value labels</h2>
    """

  override val predefinedConfigurations: List<PredefinedConfiguration<Config>> = listOf(
    PredefinedConfiguration(Config { _, gestalt ->
      gestalt.style.applyHorizontalConfiguration()
      gestalt.groupedBarsPainter.configuration.valueLabelStrokeColor = Color.white
    }, "Horizontal - Advanced"),
    PredefinedConfiguration(Config { _, gestalt ->
      gestalt.style.applyVerticalConfiguration()
      gestalt.groupedBarsPainter.configuration.valueLabelStrokeColor = Color.white
    }, "Vertical - Advanced"),

    PredefinedConfiguration(Config { _, gestalt ->
      gestalt.style.applyHorizontalConfiguration()
      gestalt.style.applyFlippingBarsValueLabelsPlacement()
    }, "Horizontal - Flipping"),
    PredefinedConfiguration(Config { _, gestalt ->
      gestalt.style.applyVerticalConfiguration()
      gestalt.style.applyFlippingBarsValueLabelsPlacement()
    }, "Vertical - Flipping"),
  )

  override val category: DemoCategory = DemoCategory.Gestalt

  override fun createDemo(configuration: PredefinedConfiguration<Config>?): ChartingDemo {
    requireNotNull(configuration) { "configuration must not be null" }

    return ChartingDemo {

      val gestalt = BarChartGroupedGestalt()
      gestalt.prepareSimpleBarChart()
      gestalt.style.showGrid = true
      gestalt.style.valueRange = ValueRange.linear(-25.0, 25.0)

      var manuallyOverwrittenValue = 30.0

      val values = listOf(
        99.9999, //replaced by seriesValueAt2
        99.9999, //replaced by seriesValueAt2 (negative!)
        0.0,
        0.1,
        -0.1,
        0.5,
        -0.5,
        1.5,
        -1.5,
        10.0,
        -10.0,
        20.0,
        -20.0,
        24.0,
        -24.0,
        24.8,
        -24.8,
      )


      //values.size categories - each with exactly one value

      gestalt.configuration.categorySeriesModel = object : CategorySeriesModel {
        override val numberOfCategories: Int = values.size
        override val numberOfSeries: Int = 1

        override fun valueAt(categoryIndex: CategoryIndex, seriesIndex: SeriesIndex): Double {
          if (categoryIndex == CategoryIndex.zero) {
            return manuallyOverwrittenValue
          }
          if (categoryIndex == CategoryIndex.one) {
            return -manuallyOverwrittenValue
          }

          return values[categoryIndex.value]
        }

        override fun categoryNameAt(categoryIndex: CategoryIndex, textService: TextService, i18nConfiguration: I18nConfiguration): String {
          return "Cat: $categoryIndex"
        }
      }

      meistercharts {
        gestalt.configure(this)

        configure {
          configurableDouble("1st bar value", manuallyOverwrittenValue) {
            min = -10.0
            max = +30.0
            onChange {
              manuallyOverwrittenValue = it
              markAsDirty()
            }
          }

          configurableValueRange("Value-range", gestalt.style::valueRange)

          configurableInsetsSeparate("Content viewport margin", gestalt::contentViewportMargin) {}
          declare {
            button("Horizontal orientation") {
              gestalt.style.applyHorizontalConfiguration()
              markAsDirty()
            }
          }

          declare {
            button("Vertical orientation") {
              gestalt.style.applyVerticalConfiguration()
              markAsDirty()
            }
          }

          configuration.payload.callback(this, gestalt)

          section("Value labels")

          configurableBoolean("Show", gestalt.groupedBarsPainter.configuration::showValueLabel)

          configurableDouble("Value Label Anchor gap Horizontal", gestalt.groupedBarsPainter.configuration::valueLabelAnchorGapHorizontal) {
            min = 0.0
            max = 100.0
          }
          configurableDouble("Value Label Anchor gap Vertical", gestalt.groupedBarsPainter.configuration::valueLabelAnchorGapVertical) {
            min = 0.0
            max = 100.0
          }

          configurableFont("Font", gestalt.groupedBarsPainter.configuration::valueLabelFont)

          configurableColorPickerNullable("Color", gestalt.groupedBarsPainter.configuration::valueLabelColor)

          configurableColorPickerNullable("Stroke color", gestalt.groupedBarsPainter.configuration::valueLabelStrokeColor)

          declare {
            section("Value Labels Box") {
              button("in Content Area") {
                gestalt.style.applyValueLabelsInContentArea()
                this@ChartingDemo.markAsDirty()
              }
              button("in Window") {
                gestalt.style.applyValueLabelsInWindow()
                this@ChartingDemo.markAsDirty()
              }
              button("in Window - Respecting Axis") {
                gestalt.style.applyValueLabelsInWindowRespectingAxis()
                this@ChartingDemo.markAsDirty()
              }
            }
          }
        }
      }
    }
  }


  class Config(
    val callback: (layerSupport: LayerSupport, gestalt: BarChartGroupedGestalt) -> Unit,
  ) {
  }

}
