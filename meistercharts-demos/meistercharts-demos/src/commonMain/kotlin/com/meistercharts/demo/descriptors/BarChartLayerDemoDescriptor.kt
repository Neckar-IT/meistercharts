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
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.barchart.CategorySeriesModelColorsProvider
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.algorithms.model.createCategoryLabelsProvider
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableSize
import com.meistercharts.demo.style
import com.meistercharts.model.Insets
import com.meistercharts.model.Orientation
import com.meistercharts.model.Side
import com.meistercharts.model.Size
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.TextKey
import it.neckar.open.observable.ObservableBoolean
import com.meistercharts.resources.Icons

/**
 */
class BarChartLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Bar Chart (deprecated)"

  //language=HTML
  override val description: String = "## Bar Chart Layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      val categories = listOf(
        Category(TextKey.simple("Bar 1")),
        Category(TextKey.simple("Bar 1.1")),
        Category(TextKey.simple("Bar 2")),
        Category(TextKey.simple("null")),
        Category(TextKey.simple("Bar 4")),
        Category(TextKey.simple("Bar -4")),
        Category(TextKey.simple("Bar 5"))
      )
      val categoryModel = DefaultCategorySeriesModel(
        categories,
        listOf(
          DefaultSeries("mySeries", listOf(1.0, 1.01, 0.7, 0.5, 0.3, -0.05, 0.1))
        )
      )
      val groupedBarsPainter = GroupedBarsPainter {
        valueRangeProvider = { ValueRange.percentage }
        barGap = 100.0
        colorsProvider = CategorySeriesModelColorsProvider.onlyCategoryColorsProvider(
          listOf(
            Color.blue,
            Color.blue,
            Color.orange,
            Color.crimson,
            Color.orangered,
            Color.orangered,
            Color.green
          )
        )
        showValueLabel = true
      }

      val categoryLayer = CategoryLayer(CategoryLayer.Data<CategorySeriesModel>() { categoryModel }) {
        categoryPainter = groupedBarsPainter
        orientation = CategoryChartOrientation.VerticalLeft
        //maxCategorySize = 30.0
        //maxCategoryDistance = 100.0
      }

      val showImages = ObservableBoolean(true)

      val categoryAxisPainter = DefaultCategoryAxisLabelPainter {
        imagesProvider = MultiProvider { index ->
          if (!showImages.get()) {
            return@MultiProvider null
          }

          when (index) {
            0 -> Icons.error(Size.PX_120)
            1 -> Icons.error(Size.PX_50)
            2 -> Icons.warning(Size.PX_90)
            else -> null
          }
        }
      }

      val categoryAxisLayer = CategoryAxisLayer(
        CategoryAxisLayer.Data(categoryModel.createCategoryLabelsProvider(), layoutProvider = { categoryLayer.paintingVariables().layout })
      ) {
        axisLabelPainter = categoryAxisPainter
        side = Side.Bottom
      }

      meistercharts {
        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(100.0))
        }

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()

          layers.addLayer(ContentAreaDebugLayer())
          layers.addLayer(categoryLayer)
          layers.addLayer(categoryAxisLayer)

          configurableDouble("Min category size", categoryLayer.style.layoutCalculator.style::minCategorySize) {
            max = 1000.0
          }

          configurableDouble("Max category size", categoryLayer.style.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 200.0
          }

          configurableEnum("Orientation", categoryLayer.style::orientation, CategoryChartOrientation.values()) {
            onChange {
              categoryAxisLayer.style.side = when (it.categoryOrientation) {
                Orientation.Vertical -> Side.Bottom
                Orientation.Horizontal -> Side.Left
              }
              markAsDirty()
            }
          }

          configurableBoolean("showImages", showImages) {
          }

          configurableBoolean("showValueLabel", groupedBarsPainter.configuration::showValueLabel) {
          }

          configurableDouble("valueLabelAnchorGap Horizontal", groupedBarsPainter.configuration::valueLabelAnchorGapHorizontal) {
            max = 100.0
          }
          configurableDouble("valueLabelAnchorGap Vertical", groupedBarsPainter.configuration::valueLabelAnchorGapVertical) {
            max = 100.0
          }

          configurableDouble("tickLabelGap", categoryAxisLayer.style::tickLabelGap) {
            max = 40.0
          }

          configurableFont(property = categoryAxisLayer.style::tickFont) {
          }

          configurableSize("Image size", categoryAxisPainter.style::imageSize)
        }
      }
    }
  }
}
