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
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.color.withAlpha
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.gesture.CanvasDragSupport
import com.meistercharts.events.gesture.connectedMouseEventHandler
import com.meistercharts.font.FontDescriptorFragment
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Distance
import it.neckar.geometry.Direction
import com.meistercharts.painter.LinePainter
import it.neckar.open.kotlin.lang.getAndSet
import it.neckar.open.kotlin.lang.sqrt
import it.neckar.open.unit.si.ms
import kotlin.reflect.KMutableProperty0

/**
 *
 */
class SplineLineInterpolationPainterDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("733a7bc1-dd28-41ce-9588-6a297386d584")
  override val name: String = "Spline Line Interpolation"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val description: String = "Spline line interpolation with two control points per segment."

  override val category: DemoCategory = DemoCategory.Painters

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        configure {
          layers.addClearBackground()

          val myLayer = object : AbstractLayer() {

            var startPoint: @Window Coordinates = Coordinates(100.0, 100.0)
            var midPoint: @Window Coordinates = Coordinates(200.0, 250.0)
            var endPoint: @Window Coordinates = Coordinates(400.0, 140.0)

            val layerRef = this

            private val canvasDragSupport = CanvasDragSupport().also { dragSupport ->
              dragSupport.handle(object : CanvasDragSupport.Handler {
                /**
                 * The property that is currently dragged
                 */
                var draggingProperty: KMutableProperty0<Coordinates>? = null

                override fun isDraggingAllowedFromHere(source: CanvasDragSupport, location: @Window Coordinates, chartSupport: ChartSupport): Boolean {
                  if (location.isCloseTo(startPoint, 5.0)) {
                    draggingProperty = layerRef::startPoint
                    return true
                  }

                  if (location.isCloseTo(midPoint, 5.0)) {
                    draggingProperty = layerRef::midPoint
                    return true
                  }

                  if (location.isCloseTo(endPoint, 5.0)) {
                    draggingProperty = layerRef::endPoint
                    return true
                  }

                  return false
                }

                override fun onDrag(source: CanvasDragSupport, @Window location: Coordinates, @Zoomed distance: Distance, @ms deltaTime: Double, chartSupport: ChartSupport): EventConsumption {
                  draggingProperty.let { property ->
                    requireNotNull(property) { "draggingProperty must not be null" }

                    property.getAndSet {
                      it + distance
                    }

                    markAsDirty()
                  }

                  return EventConsumption.Consumed
                }
              })
            }

            override val mouseEventHandler: CanvasMouseEventHandler = canvasDragSupport.connectedMouseEventHandler()

            override val type: LayerType = LayerType.Content

            var smoothingFactor: Double = 0.5

            val bigTriangleFill = Color.green.withAlpha(0.5)

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartCalculator = paintingContext.chartCalculator

              val linePainter: LinePainter = DirectLinePainter(false, false)

              linePainter.begin(gc)
              linePainter.addCoordinate(gc, startPoint)
              linePainter.addCoordinate(gc, midPoint)
              linePainter.addCoordinate(gc, endPoint)

              gc.stroke(Color.blue)
              linePainter.paint(gc)

              //Connect start + end
              gc.stroke(Color.gray)
              gc.strokeLine(startPoint, endPoint)

              //the parallel through the start point

              //delta between start + end
              @Zoomed val deltaStartEndX = endPoint.x - startPoint.x
              @Zoomed val deltaStartEndY = endPoint.y - startPoint.y

              //more deltas
              val deltaX2start = midPoint.x - startPoint.x
              val deltaX2end = midPoint.x - endPoint.x

              val deltaY2start = midPoint.y - startPoint.y
              val deltaY2end = midPoint.y - endPoint.y


              //Distance 2 start / end
              val distance2Start = (deltaX2start * deltaX2start + deltaY2start * deltaY2start).sqrt()
              val distance2End = (deltaX2end * deltaX2end + deltaY2end * deltaY2end).sqrt()

              //Calculate the factor for the "small" triangles towards the control points
              val scale2Start = smoothingFactor * distance2Start / (distance2Start + distance2End)
              val scale2End = smoothingFactor * distance2End / (distance2Start + distance2End)

              val controlStartX = midPoint.x - scale2Start * deltaStartEndX
              val controlStartY = midPoint.y - scale2Start * deltaStartEndY

              val controlEndX = midPoint.x + scale2End * deltaStartEndX
              val controlEndY = midPoint.y + scale2End * deltaStartEndY

              gc.stroke(Color.red)
              gc.strokeLine(midPoint.x, midPoint.y, controlStartX, controlStartY)

              gc.stroke(Color.green)
              gc.strokeLine(midPoint.x, midPoint.y, controlEndX, controlEndY)


              gc.beginPath()
              gc.moveTo(startPoint)
              gc.bezierCurveTo(startPoint.x, startPoint.y, controlStartX, controlStartY, midPoint.x, midPoint.y)
              gc.bezierCurveTo(controlEndX, controlEndY, endPoint.x, endPoint.y, endPoint.x, endPoint.y)

              gc.stroke(Color.aquamarine)
              gc.lineWidth = 2.0
              gc.stroke()


              //Paint the points above all other elements
              paintPoint(gc, startPoint, "S")
              paintPoint(gc, midPoint, "M")
              paintPoint(gc, endPoint, "E")
            }

            private fun paintPoint(gc: CanvasRenderingContext, location: Coordinates, label: String) {
              gc.lineWidth = 1.0

              gc.fill(Color.yellow)
              gc.stroke(Color.black)
              gc.fillOvalCenter(location, 4.0)
              gc.strokeOvalCenter(location, 4.0)

              gc.fill(Color.black)
              gc.font(FontDescriptorFragment.XS)
              gc.fillText(label, location, Direction.TopLeft, 5.0)
            }
          }
          layers.addLayer(myLayer)


          configurable("Smoothing Factor", myLayer::smoothingFactor) {
            max = 2.0
            min = -1.0
          }
        }
      }
    }
  }
}
