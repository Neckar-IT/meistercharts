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
import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.ContentAreaLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import com.meistercharts.painter.FancyPointPainter
import com.meistercharts.painter.PointPainter
import com.meistercharts.painter.RectanglePointPainter

/**
 *
 */
class XyPointPainterDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "XY Point Painter"
  override val description: String = "## Shows a point painter"
  override val category: DemoCategory = DemoCategory.Painters

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val margin = Insets(20.0, 20.0, 20.0, 70.0)

        zoomAndTranslationDefaults {
          FittingWithMargin(margin)
        }
        contentAreaSizingStrategy = BindContentAreaSize2ContentViewport()

        val valueRange = ValueRange.linear(0.0, 100.0)

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaLayer())
          layers.addLayer(ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "Binary [Boolean]" }
            paintRange = AxisStyle.PaintRange.ContentArea
            size = margin.left
            tickOrientation = Vicinity.Outside
            axisEndConfiguration = AxisEndConfiguration.Exact
          })
          val painterLayer = XYPointPainterLayer(valueRange)
          layers.addLayer(painterLayer)


          configurableList("painter style", painterLayer.style.pointPainter, availablePainters) {
            onChange {
              painterLayer.style.pointPainter = it
              markAsDirty()
            }
          }

          declare {
            section("Rectangle")
          }
          configurableDouble("line width", rectanglePointPainter::lineWidth) {
            max = 20.0
          }

          configurableColor("stroke", rectanglePointPainter::stroke) {
          }

          declare {
            section("Fancy")
          }

          configurableColor("fill", fancyPointPainter::fill) {
          }
          configurableColor("inner fill", fancyPointPainter::innerFill) {
          }
          configurableColor("outer fill", fancyPointPainter::outerFill) {
          }

          configurableDouble("point size", fancyPointPainter::pointSize) {
            max = 50.0
          }
          configurableDouble("fillSize", fancyPointPainter::fillSize) {
            min = -20.0
            max = 50.0
          }
          configurableDouble("outerWidth", fancyPointPainter::outerWidth) {
            min = -20.0
            max = 50.0
          }

        }
      }
    }
  }
}

private val rectanglePointPainter = RectanglePointPainter(false, false)
private val fancyPointPainter = FancyPointPainter(false, false)


private val availablePainters: List<PointPainter> = listOf(
  rectanglePointPainter,
  fancyPointPainter
)

class XYPointPainterLayer(
  val valueRange: ValueRange,
  styleConfigurer: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfigurer)

  override val type: LayerType
    get() = LayerType.Content

  val model: List<Double> = listOf(10.0, 20.0, 30.0, 50.0, 100.0, 99.0)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    model.forEachIndexed { i, it ->
      @DomainRelative val domainRelativeY = valueRange.toDomainRelative(it)
      val y = chartCalculator.domainRelative2windowY(domainRelativeY)
      style.pointPainter.paintPoint(gc, chartCalculator.domainRelative2windowX(0.1 * i), y)
    }
  }

  @StyleDsl
  class Style {
    var pointPainter: PointPainter = availablePainters[0]
  }
}
