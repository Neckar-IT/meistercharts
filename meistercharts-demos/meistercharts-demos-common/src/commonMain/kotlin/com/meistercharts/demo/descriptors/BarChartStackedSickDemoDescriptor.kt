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
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.style
import com.meistercharts.model.BorderRadius
import com.meistercharts.model.Insets
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.style.Palette
import com.meistercharts.zoom.FittingWithMargin
import it.neckar.geometry.Direction
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.MultiProvider

class BarChartStackedSickDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("2771824b-e46f-4e00-81ce-3e3a866a7e51")
  override val name: String = "Bar Chart Stacked - SICK"

  //language=HTML
  override val description: String = "A stacked bar chart in the SICK design"
  override val category: DemoCategory = DemoCategory.Automation
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

      meistercharts {
        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(120.0, 100.0, 100.0, 100.0))
        }

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          //layers.addLayer(ContentAreaDebugLayer())

          val categories = listOf(
            Category(TextKey.simple("LMS 151")),
            Category(TextKey.simple("LMS 511")),
            Category(TextKey.simple("OS 3")),
            Category(TextKey.simple("TDC-E")),
            Category(TextKey.simple("Weather station")),
            Category(TextKey.simple("Test"))
          )
          val series = listOf(
            DefaultSeries("OK", listOf(34.0, 47.0, 19.0, 5.0, 0.0, 0.0)),
            DefaultSeries("Warning", listOf(7.0, 10.0, 5.0, 1.0, 0.0, 0.0)),
            DefaultSeries("Error", listOf(7.0, 5.0, 3.0, 0.0, 0.0, 0.0)),
            DefaultSeries("Unknown", listOf(0.0, 0.0, 0.0, 0.0, 2.0, 0.0)),
            DefaultSeries("Zeroes", listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
          )
          val model = DefaultCategorySeriesModel(categories, series)

          val stackedBarsPainter = StackedBarsPainter().apply {
            stackedBarPaintable.configuration.valueRange = CategoryChartDemoHelper.calculateValueRangeForStackedBars(model)
            stackedBarPaintable.configuration.colorsProvider = MultiProvider.forListModuloProvider(
              listOf(
                Palette.stateSuperior,
                Palette.stateWarning,
                Palette.stateError,
                Palette.stateOffline,
                Palette.stateNormal
              )
            )
            stackedBarPaintable.configuration.segmentsGap = 4.0
            stackedBarPaintable.configuration.segmentRadii = BorderRadius.of(1.0)
            stackedBarPaintable.configuration.showRemainderAsSegment = true
            stackedBarPaintable.configuration.remainderSegmentBackgroundColor = Color.transparent
            stackedBarPaintable.configuration.remainderSegmentBorderColor = Color("#C5CACC").asProvider()
            stackedBarPaintable.configuration.remainderSegmentBorderLineWidth = 2.0
          }

          val categoryAxisDefaultPainter = DefaultCategoryAxisLabelPainter()
          val categoryAxisPainters = listOf(categoryAxisDefaultPainter, DebugCategoryAxisLabelPainter())

          val categoryLayer = CategoryLayer<CategorySeriesModel>(CategoryLayer.Configuration<CategorySeriesModel> { model }) {
            categoryPainter = stackedBarsPainter
          }
          layers.addLayer(categoryLayer)

          val categoryAxisLayer = categoryLayer.createAxisLayer {
            axisLabelPainter = categoryAxisDefaultPainter
            size = 102.0
            axisLineWidth = 2.0
            lineColor = Color("#C5CACC").asProvider()
            hideTicks()
          }
          layers.addLayer(categoryAxisLayer)

          configurable("Orientation", categoryLayer.configuration::orientation, kotlin.enums.enumEntries<CategoryChartOrientation>().toList()) {
            onChange {
              categoryAxisLayer.configuration.side = when (it.categoryOrientation) {
                Orientation.Vertical -> Side.Bottom
                Orientation.Horizontal -> Side.Left
              }
              markAsDirty()
            }
          }

          configurable("Min category size", categoryLayer.configuration.layoutCalculator.style::minCategorySize, {
            max = 1000.0
          })

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

          section("Grouped Bars Painter")

          configurable("Bar gap", stackedBarsPainter.stackedBarPaintable.style::segmentsGap) {
            min = -10.0
            max = 100.0
          }

          configurable("Show value labels", stackedBarsPainter.stackedBarPaintable.style::showValueLabels) {
          }

          configurable("Value labels font", stackedBarsPainter.stackedBarPaintable.style::valueLabelFont) {
          }

          configurable("Value labels color", stackedBarsPainter.stackedBarPaintable.style::valueLabelColor) {
          }

          configurable("Value labels gap Horizontal", stackedBarsPainter.stackedBarPaintable.style::valueLabelGapHorizontal) {
            max = 100.0
          }
          configurable("Value labels gap Vertical", stackedBarsPainter.stackedBarPaintable.style::valueLabelGapVertical) {
            max = 100.0
          }

          configurable("Value labels anchor direction", stackedBarsPainter.stackedBarPaintable.style::valueLabelAnchorDirection, kotlin.enums.enumEntries<Direction>().toList()) {
          }

          section("Category Axis")

          configurable("Category axis font", categoryAxisLayer.configuration::tickFont) {
          }
        }
      }
    }
  }

}
