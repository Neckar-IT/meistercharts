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

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.barchart.DebugCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter
import com.meistercharts.algorithms.layers.barchart.createAxisLayer
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.style
import com.meistercharts.model.Insets
import com.meistercharts.model.Orientation
import com.meistercharts.model.Side
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.enumEntries

class CategoryChartLayerGroupedDemoDescriptor : ChartingDemoDescriptor<DefaultCategorySeriesModel> {
  override val name: String = "Category Chart : Grouped"

  //language=HTML
  override val description: String = "## Category Chart Layer with grouped bars"
  override val category: DemoCategory = DemoCategory.Layers

  override val predefinedConfigurations: List<PredefinedConfiguration<DefaultCategorySeriesModel>> = buildList {
    val categories = listOf(
      Category(TextKey("2018", "2018")),
      Category(TextKey("2019", "2019")),
      Category(TextKey("2020", "2020"))
    )

    add(
      PredefinedConfiguration(
        DefaultCategorySeriesModel(
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
        DefaultCategorySeriesModel(
          categories, listOf(
            DefaultSeries("Gorillas", listOf(-10.0, 0.0, -12.0)),
            DefaultSeries("Giraffen", listOf(-5.0, -3.0, -5.0)),
            DefaultSeries("Erdmännchen", listOf(-20.0, -8.0, -4.0)),
            DefaultSeries("Zebras", listOf(-4.0, -3.0, -3.0))
          )
        ), "Only negative"
      )
    )
    add(
      PredefinedConfiguration(
        DefaultCategorySeriesModel(
          categories, listOf(
            DefaultSeries("Gorillas", listOf(-10.0, 0.0, 12.0)),
            DefaultSeries("Giraffen", listOf(-5.0, -3.0, 5.0)),
            DefaultSeries("Erdmännchen", listOf(-20.0, 8.0, 4.0)),
            DefaultSeries("Zebras", listOf(-4.0, -3.0, 3.0))
          )
        ), "Positive/Negative"
      )
    )


  }

  override fun createDemo(configuration: PredefinedConfiguration<DefaultCategorySeriesModel>?): ChartingDemo {
    require(configuration != null) { "Config required" }

    val model = configuration.payload

    return ChartingDemo {

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

          val layer = CategoryLayer(CategoryLayer.Data<CategorySeriesModel> { model }) {
            categoryPainter = groupedBarsPainter
          }
          layers.addLayer(layer)

          val categoryAxisLayer = layer.createAxisLayer {
            axisLabelPainter = defaultCategoryAxisPainter
          }
          layers.addLayer(categoryAxisLayer)

          configurableEnum("Orientation", layer.style::orientation, enumEntries()) {
            onChange {
              categoryAxisLayer.style.side = when (it.categoryOrientation) {
                Orientation.Vertical -> Side.Bottom
                Orientation.Horizontal -> Side.Left
              }
              markAsDirty()
            }
          }

          configurableDouble("Min category size", layer.style.layoutCalculator.style::minCategorySize, {
            max = 1000.0
          })

          configurableDouble("Max category size", layer.style.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 1000.0
          }

          configurableList("Axis Painter", categoryAxisLayer.style.axisLabelPainter, axisPainters) {
            this.converter = { painter ->
              painter::class.simpleName ?: painter.toString()
            }
            onChange {
              categoryAxisLayer.style.axisLabelPainter = it
              markAsDirty()
            }
          }

          declare {
            section("Grouped Bars Painter")
          }

          configurableDouble("Bar gap", groupedBarsPainter.configuration::barGap) {
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

          configurableBoolean("Show value labels", groupedBarsPainter.configuration::showValueLabel) {
          }

          configurableFont("Value labels font", groupedBarsPainter.configuration::valueLabelFont) {
          }

          configurableColorPickerNullable("Value labels color", groupedBarsPainter.configuration::valueLabelColor)

          configurableColorPickerNullable("Value labels stroke color", groupedBarsPainter.configuration::valueLabelStrokeColor)

          configurableDouble("Value labels gap Horizontal", groupedBarsPainter.configuration::valueLabelAnchorGapHorizontal) {
            max = 100.0
          }
          configurableDouble("Value labels gap Vertical", groupedBarsPainter.configuration::valueLabelAnchorGapVertical) {
            max = 100.0
          }

          declare {
            section("Category Axis")
          }

          configurableFont("Category axis font", categoryAxisLayer.style::tickFont) {
          }
        }
      }
    }
  }

}

