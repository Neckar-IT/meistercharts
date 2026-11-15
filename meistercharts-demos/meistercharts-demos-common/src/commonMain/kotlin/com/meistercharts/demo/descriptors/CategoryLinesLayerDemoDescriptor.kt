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
import com.meistercharts.algorithms.layers.CategoryLinesLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.crosswire.LabelPlacementStrategy
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.SeriesIndex
import com.meistercharts.color.Color
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableValueRange
import com.meistercharts.painter.DotCategoryPointPainter
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

/**
 * Demonstrates category lines and points
 */
class CategoryLinesLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("44155728-9476-4527-8abd-816da2782cf5")
  override val name: String = "Category Lines Layer"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

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

    return MeisterchartsDemo(this) {
      val colors = listOf(Color.orangered, Color.blueviolet, Color.greenyellow)

      val categoryLinesLayer = CategoryLinesLayer(categorySeriesModel)
      categoryLinesLayer.configuration.valueRange = LinearValueRange(-30.0, 50.0)
      categoryLinesLayer.configuration.lineStyles = MultiProvider.forListModulo(colors.map { color -> LineStyle(color) })
      categoryLinesLayer.configuration.pointPainters = MultiProvider.forListModulo(colors.map { color -> DotCategoryPointPainter(snapXValues = false, snapYValues = false).apply { pointStylePainter.color = color } })

      meistercharts {
        configure {

          layers.addClearBackground()
          layers.addLayer(categoryLinesLayer)

          configurable("Value range", categoryLinesLayer.configuration::valueRange)

          configurableList(
            "Label-placement strategy", categoryLinesLayer.configuration.valueLabelPlacementStrategy,
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
              categoryLinesLayer.configuration.valueLabelPlacementStrategy = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
