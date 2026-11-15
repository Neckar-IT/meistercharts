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
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.range.ValueRange
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.zoom.FittingWithMargin
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.ContentAreaLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorProvider
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
class XyPointPainterDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("8fba2734-207e-477f-9379-162f9f16762f")
  override val name: String = "XY Point Painter"
  override val description: String = "Shows different point painters along a value axis."
  override val category: DemoCategory = DemoCategory.Painters
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
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
          layers.addLayer(ValueAxisLayer(ValueAxisLayer.Configuration(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "Binary [Boolean]" }
            paintRange = AxisConfiguration.PaintRange.ContentArea
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

          section("Rectangle")
          configurable("line width", rectanglePointPainter::lineWidth) {
            max = 20.0
          }

          configurableColorProvider("stroke", rectanglePointPainter::stroke) {
          }

          section("Fancy")

          configurableColorProvider("fill", fancyPointPainter::fill) {
          }
          configurableColorProvider("inner fill", fancyPointPainter::innerFill) {
          }
          configurableColorProvider("outer fill", fancyPointPainter::outerFill) {
          }

          configurable("point size", fancyPointPainter::pointSize) {
            max = 50.0
          }
          configurable("fillSize", fancyPointPainter::fillSize) {
            min = -20.0
            max = 50.0
          }
          configurable("outerWidth", fancyPointPainter::outerWidth) {
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
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

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
