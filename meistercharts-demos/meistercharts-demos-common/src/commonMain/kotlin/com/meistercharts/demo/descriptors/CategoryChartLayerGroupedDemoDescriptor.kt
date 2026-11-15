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

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.barchart.DebugCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter
import com.meistercharts.algorithms.layers.barchart.createAxisLayer
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurable
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerProviderNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section
import com.meistercharts.demo.style
import com.meistercharts.model.Insets
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.zoom.FittingWithMargin
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import it.neckar.open.i18n.TextKey
import kotlin.uuid.Uuid

class CategoryChartLayerGroupedDemoDescriptor : MeisterchartsDemoDescriptor<DefaultCategorySeriesModel> {
  override val uuid: Uuid = Uuid.parse("30f35821-38fc-4099-9906-fae96994603c")
  override val name: String = "Category Chart : Grouped"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Category Chart Layer with grouped bars"
  override val category: DemoCategory = DemoCategory.Layers

  override val predefinedConfigurations: List<PredefinedConfiguration<DefaultCategorySeriesModel>> = buildList {
    val categories = listOf(
      Category(TextKey("2018", "2018")),
      Category(TextKey("2019", "2019")),
      Category(TextKey("2020", "2020"))
    )

    add(
      PredefinedConfiguration(
        uuid = Uuid.parse("ce6a9341-2c22-4559-8109-e1361332724d"),
        name = "DefaultCategorySeriesModel",
        payload = DefaultCategorySeriesModel(
          categories, listOf(
            DefaultSeries("Gorillas", listOf(10.0, 0.0, 12.0)),
            DefaultSeries("Giraffen", listOf(5.0, 3.0, 5.0)),
            DefaultSeries("Erdmännchen", listOf(20.0, 8.0, 4.0)),
            DefaultSeries("Zebras", listOf(4.0, 3.0, 3.0))
          )
        )
      )
    )

    add(
      PredefinedConfiguration(
        uuid = Uuid.parse("8505e64b-2fb6-49db-96bb-659db22d65e4"),
        payload = DefaultCategorySeriesModel(
          categories, listOf(
            DefaultSeries("Gorillas", listOf(-10.0, 0.0, -12.0)),
            DefaultSeries("Giraffen", listOf(-5.0, -3.0, -5.0)),
            DefaultSeries("Erdmännchen", listOf(-20.0, -8.0, -4.0)),
            DefaultSeries("Zebras", listOf(-4.0, -3.0, -3.0))
          )
        ), name = "Only negative"
      )
    )
    add(
      PredefinedConfiguration(
        uuid = Uuid.parse("08141cb6-7e0e-4c17-8f38-ca48e2ed1e8c"),
        payload = DefaultCategorySeriesModel(
          categories, listOf(
            DefaultSeries("Gorillas", listOf(-10.0, 0.0, 12.0)),
            DefaultSeries("Giraffen", listOf(-5.0, -3.0, 5.0)),
            DefaultSeries("Erdmännchen", listOf(-20.0, 8.0, 4.0)),
            DefaultSeries("Zebras", listOf(-4.0, -3.0, 3.0))
          )
        ), name = "Positive/Negative"
      )
    )


  }

  override fun prepareDemo(configuration: PredefinedConfiguration<DefaultCategorySeriesModel>?): MeisterchartsDemo {
    require(configuration != null) { "Config required" }

    val model = configuration.payload

    return MeisterchartsDemo(this) {

      meistercharts {
        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(100.0))
        }

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          val groupedBarsPainter = GroupedBarsPainter {
            valueRangeProvider = { CategoryChartDemoHelper.calculateValueRangeForGroupedBars(model) }
          }

          val defaultCategoryAxisPainter = DefaultCategoryAxisLabelPainter()
          val axisPainters = listOf(defaultCategoryAxisPainter, DebugCategoryAxisLabelPainter())

          val layer = CategoryLayer(CategoryLayer.Configuration<CategorySeriesModel> { model }) {
            categoryPainter = groupedBarsPainter
          }
          layers.addLayer(layer)

          val categoryAxisLayer = layer.createAxisLayer {
            axisLabelPainter = defaultCategoryAxisPainter
          }
          layers.addLayer(categoryAxisLayer)

          configurable("Orientation", layer.configuration::orientation, kotlin.enums.enumEntries<CategoryChartOrientation>().toList()) {
            onChange {
              categoryAxisLayer.configuration.side = when (it.categoryOrientation) {
                Orientation.Vertical -> Side.Bottom
                Orientation.Horizontal -> Side.Left
              }
              markAsDirty()
            }
          }

          configurable("Min category size", layer.configuration.layoutCalculator.style::minCategorySize, {
            max = 1000.0
          })

          configurable("Max category size", layer.configuration.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 1000.0
          }

          configurableList("Axis Painter", categoryAxisLayer.configuration.axisLabelPainter, axisPainters) {
            this.converter = { painter ->
              painter::class.simpleName ?: painter.toString()
            }
            onChange {
              categoryAxisLayer.configuration.axisLabelPainter = it
              markAsDirty()
            }
          }

          section("Grouped Bars Painter")

          configurable("Bar gap", groupedBarsPainter.configuration::barGap) {
            min = -10.0
            max = 100.0
          }

          configurableDouble("Min bar size", groupedBarsPainter.configuration.minBarSize) {
            min = 1.0
            max = 200.0
            onChange {
              groupedBarsPainter.configuration.setBarSizeRange(it, groupedBarsPainter.configuration.maxBarSize)
              markAsDirty()
            }
          }

          configurableDouble("Max bar size", groupedBarsPainter.configuration.maxBarSize ?: 200.0) {
            min = 1.0
            max = 200.0
            onChange {
              groupedBarsPainter.configuration.setBarSizeRange(groupedBarsPainter.configuration.minBarSize, it)
              markAsDirty()
            }
          }

          configurable("Show value labels", groupedBarsPainter.configuration::showValueLabel) {
          }

          configurable("Value labels font", groupedBarsPainter.configuration::valueLabelFont) {
          }

          configurable("Value labels color", groupedBarsPainter.configuration::valueLabelColor)

          configurable("Value labels stroke color", groupedBarsPainter.configuration::valueLabelStrokeColor)

          configurable("Value labels gap Horizontal", groupedBarsPainter.configuration::valueLabelAnchorGapHorizontal) {
            max = 100.0
          }
          configurable("Value labels gap Vertical", groupedBarsPainter.configuration::valueLabelAnchorGapVertical) {
            max = 100.0
          }

          section("Category Axis")

          configurable("Category axis font", categoryAxisLayer.configuration::tickFont) {
          }
        }
      }
    }
  }

}

