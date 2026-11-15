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
import com.meistercharts.demo.configurableSeparate
import kotlin.uuid.Uuid
import com.meistercharts.canvas.layer.LayerSupport
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerProviderNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableValueRange
import com.meistercharts.demo.section
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.SeriesIndex
import com.meistercharts.range.ValueRange
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

class BarValueLabelDemoDescriptor : MeisterchartsDemoDescriptor<BarValueLabelDemoDescriptor.Config> {
  override val uuid: Uuid = Uuid.parse("81bd6bed-6952-436a-872f-d53bf519da1d")

  override val name: String = "Bar chart with value labels"

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "A bar chart with value labels"

  override val predefinedConfigurations: List<PredefinedConfiguration<Config>> = listOf(
    PredefinedConfiguration(Uuid.parse("fa40b273-a266-46cf-bde0-61a279871757"), Config { _, gestalt ->
      gestalt.configuration.applyHorizontalConfiguration()
      gestalt.groupedBarsPainter.configuration.valueLabelStrokeColor = Color.white
    }, "Horizontal - Advanced"),
    PredefinedConfiguration(Uuid.parse("b06fb6fa-8ec3-4efd-8bd6-2f11ffd5fe31"), Config { _, gestalt ->
      gestalt.configuration.applyVerticalConfiguration()
      gestalt.groupedBarsPainter.configuration.valueLabelStrokeColor = Color.white
    }, "Vertical - Advanced"),

    PredefinedConfiguration(Uuid.parse("73d25150-eea8-4c86-9e2a-35ffbd12e320"), Config { _, gestalt ->
      gestalt.configuration.applyHorizontalConfiguration()
      gestalt.configuration.applyFlippingBarsValueLabelsPlacement()
    }, "Horizontal - Flipping"),
    PredefinedConfiguration(Uuid.parse("bac96c37-699e-477a-98f9-fdf6ea061049"), Config { _, gestalt ->
      gestalt.configuration.applyVerticalConfiguration()
      gestalt.configuration.applyFlippingBarsValueLabelsPlacement()
    }, "Vertical - Flipping"),
  )

  override val category: DemoCategory = DemoCategory.Gestalt

  override fun prepareDemo(configuration: PredefinedConfiguration<Config>?): MeisterchartsDemo {
    requireNotNull(configuration) { "configuration must not be null" }

    return MeisterchartsDemo(this) {

      val gestalt = BarChartGroupedGestalt()
      gestalt.prepareSimpleBarChart()
      gestalt.configuration.showGrid = true
      gestalt.configuration.valueRange = ValueRange.linear(-25.0, 25.0)

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

          configurable("Value-range", gestalt.configuration::valueRange)

          configurableSeparate("Content viewport margin", gestalt::contentViewportMargin) {}
          declare {
            button("Horizontal orientation") {
              gestalt.configuration.applyHorizontalConfiguration()
              markAsDirty()
            }
          }

          declare {
            button("Vertical orientation") {
              gestalt.configuration.applyVerticalConfiguration()
              markAsDirty()
            }
          }

          configuration.payload.callback(this, gestalt)

          section("Value labels")

          configurable("Show", gestalt.groupedBarsPainter.configuration::showValueLabel)

          configurable("Value Label Anchor gap Horizontal", gestalt.groupedBarsPainter.configuration::valueLabelAnchorGapHorizontal) {
            min = 0.0
            max = 100.0
          }
          configurable("Value Label Anchor gap Vertical", gestalt.groupedBarsPainter.configuration::valueLabelAnchorGapVertical) {
            min = 0.0
            max = 100.0
          }

          configurable("Font", gestalt.groupedBarsPainter.configuration::valueLabelFont)

          configurable("Color", gestalt.groupedBarsPainter.configuration::valueLabelColor)
          configurable("Stroke color", gestalt.groupedBarsPainter.configuration::valueLabelStrokeColor)

          declare {
            section("Value Labels Box")
            button("in Content Area") {
              gestalt.configuration.applyValueLabelsInContentArea()
              this@MeisterchartsDemo.markAsDirty()
            }
            button("in Window") {
              gestalt.configuration.applyValueLabelsInWindow()
              this@MeisterchartsDemo.markAsDirty()
            }
            button("in Window - Respecting Axis") {
              gestalt.configuration.applyValueLabelsInWindowRespectingAxis()
              this@MeisterchartsDemo.markAsDirty()
            }
          }
        }
      }
    }
  }


  class Config(
    val callback: (layerSupport: LayerSupport, gestalt: BarChartGroupedGestalt) -> Unit,
  )
}
