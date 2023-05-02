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
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableValueRange
import com.meistercharts.model.Insets
import com.meistercharts.model.Side
import it.neckar.open.i18n.TextKey

/**
 *
 */
class GroupedBarChartClippingDemoDescriptor : ChartingDemoDescriptor<Any> {
  override val name: String = "Grouped Bar Chart Clipping"
  override val category: DemoCategory = DemoCategory.BugHunting

  override fun createDemo(configuration: PredefinedConfiguration<Any>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val categoryModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("Negative Values")),
            Category(TextKey.simple("Positive Values")),
            Category(TextKey.simple("Negative Values")),
            Category(TextKey.simple("Positive Values")),
            Category(TextKey.simple("Negative Values")),
          ),
          listOf(
            DefaultSeries("1", listOf(-0.9, 1.1, -7.8, 0.5, -1.0)),
            DefaultSeries("2", listOf(-1.1, 2.5, -3.6, 4.7, -0.1)),
            DefaultSeries("3", listOf(-8.2, 3.5, -4.6, 5.7, -0.1)),
            DefaultSeries("4", listOf(-0.5, 4.5, -5.6, 6.7, -2.0)),
          )
        )

        val gestalt = BarChartGroupedGestalt(categoryModel)
        gestalt.configure(this)
        gestalt.style.valueRange = ValueRange.linear(-1.0, 10.0)

        gestalt.style.applyHorizontalConfiguration()
        gestalt.categoryAxisLayer.style.side = Side.Right
        gestalt.contentViewportMargin = Insets.of(150.0)

        configure {
          layers.addLayer(ContentAreaDebugLayer())
        }

        configurableInsetsSeparate("content area margin", gestalt::contentViewportMargin)
        configurableValueRange("Value Range", gestalt.style::valueRange)
      }
    }
  }
}
