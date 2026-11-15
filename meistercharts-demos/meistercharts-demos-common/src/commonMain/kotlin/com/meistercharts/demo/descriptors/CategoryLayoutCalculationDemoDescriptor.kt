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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.DefaultCategoryLayouter
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.color.Color
import com.meistercharts.annotations.Zoomed
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.text.LineSpacing
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.stroke
import com.meistercharts.canvas.strokeRectCoordinates
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableDoubleProvider
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import it.neckar.geometry.Direction
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Orientation
import it.neckar.open.formatting.decimalFormat
import com.meistercharts.style.BoxStyle

/**
 */
class CategoryLayoutCalculationDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("5c47e692-80f8-4275-a03a-de70ded246ba")
  override val name: String = "Category Layout Calculation"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          val categoryLayoutLayer = MyCategoryLayoutLayer()
          layers.addLayer(categoryLayoutLayer)

          configurable("Orientation", categoryLayoutLayer.configuration::orientation, kotlin.enums.enumEntries<CategoryChartOrientation>().toList())
          configurable("Number of segments", categoryLayoutLayer.configuration::numberOfSegments) {
            max = 17
          }

          configurable("Min category size", categoryLayoutLayer.layouter.style::minCategorySize) {
            max = 1000.0
          }

          configurable("Max category size", categoryLayoutLayer.layouter.style::maxCategorySize, 150.0) {
            max = 1000.0
          }

          configurable("Gap Size", categoryLayoutLayer.layouter.style::gapSize) {
            max = 1000.0
          }
        }
      }
    }
  }
}

private class MyCategoryLayoutLayer(
  val configuration: Configuration = Configuration(),
  additionalConfiguration: Configuration.() -> Unit = {}
) : AbstractLayer() {

  override val type: LayerType = LayerType.Content

  init {
    configuration.additionalConfiguration()
  }

  val layouter: DefaultCategoryLayouter = DefaultCategoryLayouter()

  override fun paint(paintingContext: LayerPaintingContext) {
    val segmentsLayout = layouter.calculateLayout(paintingContext, configuration.numberOfSegments, configuration.orientation)

    val gc = paintingContext.gc
    gc.font(FontDescriptorFragment.DefaultSize)

    gc.paintTextBox(
      listOf(
        "Available space: ${decimalFormat.format(segmentsLayout.availableSpace)}",
        "Remaining space: ${decimalFormat.format(segmentsLayout.remainingSpace)}",
        "used space: ${decimalFormat.format(segmentsLayout.usedSpace)}",
        "segment size: ${decimalFormat.format(segmentsLayout.boxSize)}",
        "layout direction: ${segmentsLayout.layoutDirection}"
      ), LineSpacing.Single, HorizontalAlignment.Left, Direction.TopLeft, 10.0, boxStyle = BoxStyle.gray, textColor = Color.black()
    )

    for (i in 0 until configuration.numberOfSegments) {
      val boxIndex = BoxIndex(i)
      val start = segmentsLayout.calculateStart(boxIndex)
      val end = segmentsLayout.calculateEnd(boxIndex)


      val startY: @Zoomed Double
      val endY: @Zoomed Double
      val startX: @Zoomed Double
      val endX: @Zoomed Double

      when (configuration.orientation.categoryOrientation) {
        Orientation.Vertical -> {
          startY = 205.0
          endY = startY + 100.0
          startX = start
          endX = end
        }

        Orientation.Horizontal -> {
          startX = 205.0
          endX = startX + 100.0
          startY = start
          endY = end
        }
      }

      gc.stroke(Color.orange)
      gc.strokeRectCoordinates(startX, startY, endX, endY)
      gc.strokeLine(startX, startY, endX, endY)
      gc.strokeLine(startX, endY, endX, startY)

      gc.paintMark((startX + endX) / 2.0, (startY + endY) / 2.0, color = Color.red())
    }
  }

  @ConfigurationDsl
  class Configuration {
    var numberOfSegments = 7
    var orientation = CategoryChartOrientation.HorizontalBottom
  }
}
