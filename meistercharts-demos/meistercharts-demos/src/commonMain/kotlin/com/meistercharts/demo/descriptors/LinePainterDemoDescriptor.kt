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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.ContentAreaLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.algorithms.painter.SplineLinePainter
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Insets
import com.meistercharts.painter.CirclePointPainter
import com.meistercharts.painter.LinePainter
import it.neckar.open.kotlin.lang.fastMap

/**
 *
 */
class LinePainterDemoDescriptor : ChartingDemoDescriptor<LinePainter> {
  override val name: String = "Line Painter"
  override val category: DemoCategory = DemoCategory.Painters

  override val predefinedConfigurations: List<PredefinedConfiguration<LinePainter>> = listOf(
    PredefinedConfiguration(DirectLinePainter(false, false), "Direct"),
    PredefinedConfiguration(SplineLinePainter(false, false), "Spline"),
  )

  override fun createDemo(configuration: PredefinedConfiguration<LinePainter>?): ChartingDemo {
    requireNotNull(configuration) { "Configuration required" }

    val linePainter = configuration.payload

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
          val linePainterLayer = MyLinePainterLayer(valueRange, linePainter)
          layers.addLayer(linePainterLayer)

          if (linePainter is SplineLinePainter) {
            configurableDouble("Smoothing factor", linePainter::smoothingFactor) {
              min = -2.0
              max = 2.0
            }
          }

          configurableInt("point count", linePainterLayer::pointCount) {
            min = 1
            max = 10
          }

          configurableDouble("line width", linePainterLayer.style::lineWidth) {
            max = 20.0
          }

          configurableColorPicker("line Color", linePainterLayer.style::lineColor)

          configurableBoolean("Show Points", linePainterLayer.style::showPoints)
        }
      }
    }
  }
}

class MyLinePainterLayer(
  val valueRange: ValueRange,
  val linePainter: LinePainter,
  styleConfigurer: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfigurer)

  override val type: LayerType
    get() = LayerType.Content

  var pointCount: Int = 6

  val pointPainter: CirclePointPainter = CirclePointPainter(false, false)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    val linePainter = linePainter
    linePainter.begin(gc)

    val model = pointCount.fastMap { value -> 50.0 + if (value % 2 == 0) -5.0 else 5.0 * value }

    model.forEachIndexed { index, it ->
      @DomainRelative val domainRelativeY = valueRange.toDomainRelative(it)
      @Window val y = chartCalculator.domainRelative2windowY(domainRelativeY)
      @Window val x = chartCalculator.domainRelative2windowX(0.1 + 0.1 * index)

      //TODO line segment style!
      linePainter.addCoordinate(gc, x, y)
    }

    gc.lineWidth = style.lineWidth
    gc.strokeStyle(style.lineColor)

    linePainter.finish(gc)

    if (style.showPoints) {
      model.forEachIndexed { index, it ->
        @DomainRelative val domainRelativeY = valueRange.toDomainRelative(it)
        @Window val y = chartCalculator.domainRelative2windowY(domainRelativeY)
        @Window val x = chartCalculator.domainRelative2windowX(0.1 + 0.1 * index)

        pointPainter.paintPoint(gc, x, y)
      }
    }
  }

  @StyleDsl
  class Style {
    var showPoints: Boolean = false

    var lineWidth: Double = 1.0
    var lineColor: Color = Color.red
  }
}
