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
import com.meistercharts.algorithms.layers.CategoryLinesLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.crosswire.LabelPlacementStrategy
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.SeriesIndex
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableValueRange
import com.meistercharts.painter.DotCategoryPointPainter
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

/**
 * Demonstrates category lines and points
 */
class CategoryLinesLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Category Lines Layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    val categorySeriesModel: CategorySeriesModel = object : CategorySeriesModel {
      override val numberOfCategories: Int = 20
      override val numberOfSeries: Int = 3

      override fun valueAt(categoryIndex: CategoryIndex, seriesIndex: SeriesIndex): Double {
        return when (seriesIndex.value) {
          0 -> {
            when (categoryIndex.value) {
              0 -> -29.9
              numberOfCategories - 1 -> 49.9
              else -> 10.00001 - categoryIndex.value.toDouble()
            }
          }

          1 -> 5.00001 + categoryIndex.value.toDouble()
          2 -> 5.00001 + categoryIndex.value.toDouble() % 3.0
          else -> throw UnsupportedOperationException("unsupported series index ${seriesIndex.value}")
        }
      }

      override fun categoryNameAt(categoryIndex: CategoryIndex, textService: TextService, i18nConfiguration: I18nConfiguration): String {
        return "Category ${categoryIndex.value}"
      }
    }

    return ChartingDemo {
      val colors = listOf(Color.orangered, Color.blueviolet, Color.greenyellow)
      val textColors = listOf(Color.blue, Color.white, Color.black)

      val categoryLinesLayer = CategoryLinesLayer(CategoryLinesLayer.Data(categorySeriesModel))
      categoryLinesLayer.style.valueRange = LinearValueRange(-30.0, 50.0)
      categoryLinesLayer.style.lineStyles = MultiProvider.forListModulo(colors.map { color -> LineStyle(color) })
      categoryLinesLayer.style.pointPainters = MultiProvider.forListModulo(colors.map { color -> DotCategoryPointPainter(snapXValues = false, snapYValues = false).apply { pointStylePainter.color = color } })

      meistercharts {
        configure {

          layers.addClearBackground()
          layers.addLayer(categoryLinesLayer)

          configurableValueRange("Value range", categoryLinesLayer.style::valueRange)

          configurableList(
            "Label-placement strategy", categoryLinesLayer.style.valueLabelPlacementStrategy,
            listOf(
              LabelPlacementStrategy.AlwaysOnRightSide,
              LabelPlacementStrategy.AlwaysOnLeftSide,
              LabelPlacementStrategy.preferOnRightSide { 150.0 },
            )
          ) {
            converter {
              when (it) {
                LabelPlacementStrategy.AlwaysOnRightSide -> "always on right"
                LabelPlacementStrategy.AlwaysOnLeftSide -> "always on left"
                else -> "prefer on right"
              }
            }

            onChange {
              categoryLinesLayer.style.valueLabelPlacementStrategy = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
