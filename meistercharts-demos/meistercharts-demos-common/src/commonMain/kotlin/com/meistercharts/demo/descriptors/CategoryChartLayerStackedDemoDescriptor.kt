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
import com.meistercharts.demo.section
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.barchart.DebugCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.StackedBarsPainter
import com.meistercharts.algorithms.layers.barchart.createAxisLayer
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerProviderNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.style
import com.meistercharts.model.Insets
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.zoom.FittingWithMargin
import it.neckar.geometry.Direction
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.TextKey

class CategoryChartLayerStackedDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("6d53a289-3e57-4365-9d00-ca5b95d88ff2")
  override val name: String = "Category Chart : Stacked"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "The category chart layer with stacked bars"
  override val category: DemoCategory = DemoCategory.Layers

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

      meistercharts {
        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(100.0))
        }

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          val categories = listOf(
            Category(TextKey("2018", "2018")),
            Category(TextKey("2019", "2019")),
            Category(TextKey("2020", "2020"))
          )
          val series = listOf(
            DefaultSeries("Gorillas", listOf(10.0, 0.0, 12.0)),
            DefaultSeries("Giraffen", listOf(5.0, 3.0, 5.0)),
            DefaultSeries("Erdmännchen", listOf(20.0, 8.0, 4.0)),
            DefaultSeries("Zebras", listOf(4.0, 3.0, 3.0))
          )
          val model = DefaultCategorySeriesModel(categories, series)

          val stackedBarsPainter = StackedBarsPainter().apply {
            stackedBarPaintable.configuration.valueRange = CategoryChartDemoHelper.calculateValueRangeForStackedBars(model)
          }

          val categoryAxisDefaultPainter = DefaultCategoryAxisLabelPainter()
          val categoryAxisPainters = listOf(categoryAxisDefaultPainter, DebugCategoryAxisLabelPainter())

          val categoryLayer = CategoryLayer(CategoryLayer.Configuration<CategorySeriesModel> { model }) {
            categoryPainter = stackedBarsPainter
          }
          layers.addLayer(categoryLayer)

          val categoryAxisLayer = categoryLayer.createAxisLayer {
            axisLabelPainter = categoryAxisDefaultPainter
          }
          layers.addLayer(categoryAxisLayer)

          configurable("Orientation", categoryLayer.configuration::orientation, kotlin.enums.enumEntries<CategoryChartOrientation>().toList()) {
            onChange {
              categoryAxisLayer.configuration.side = when (it.categoryOrientation) {
                Orientation.Vertical -> Side.Bottom
                Orientation.Horizontal -> Side.Left
              }
            }
          }

          configurable("Min category size", categoryLayer.configuration.layoutCalculator.style::minCategorySize) {
            max = 1000.0
          }

          configurable("Max category size", categoryLayer.configuration.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 1000.0
          }

          configurableList("Axis Painter", categoryAxisLayer.configuration.axisLabelPainter, categoryAxisPainters) {
            this.converter = { painter ->
              painter::class.simpleName ?: painter.toString()
            }
            onChange {
              categoryAxisLayer.configuration.axisLabelPainter = it
              markAsDirty()
            }
          }

          section("Bars Painter")

          configurable("Segments gap", stackedBarsPainter.stackedBarPaintable.style::segmentsGap) {
            min = -10.0
            max = 100.0
          }

          configurable("Max bar size", stackedBarsPainter.style::maxBarSize) {
            max = 100.0
          }

          configurable("Show value labels", stackedBarsPainter.stackedBarPaintable.style::showValueLabels)

          configurable("Max label width", stackedBarsPainter.stackedBarPaintable.style::maxValueLabelWidth, 200.0) {
            max = 500.0
          }

          configurableInt("Value labels decimal places") {
            max = 15
            min = 0
            value = 0

            onChange {
              stackedBarsPainter.stackedBarPaintable.configuration.valueLabelFormat = decimalFormat(it)
              markAsDirty()
            }
          }

          configurableFont("Value labels font", stackedBarsPainter.stackedBarPaintable.configuration.valueLabelFont) {
            onChange {
              stackedBarsPainter.stackedBarPaintable.configuration.valueLabelFont = it
              markAsDirty()
            }
          }

          configurable("Value labels color", stackedBarsPainter.stackedBarPaintable.style::valueLabelColor) {
          }

          configurable("Value labels gap Horizontal", stackedBarsPainter.stackedBarPaintable.style::valueLabelGapHorizontal) {
            max = 100.0
          }
          configurable("Value labels gap Vertical", stackedBarsPainter.stackedBarPaintable.style::valueLabelGapVertical) {
            max = 100.0
          }

          configurable("Value labels anchor direction", stackedBarsPainter.stackedBarPaintable.style::valueLabelAnchorDirection, kotlin.enums.enumEntries<Direction>().toList())

          section("Category Axis")

          configurable("Category axis font", categoryAxisLayer.configuration::tickFont) {
          }

        }
      }
    }
  }

}
