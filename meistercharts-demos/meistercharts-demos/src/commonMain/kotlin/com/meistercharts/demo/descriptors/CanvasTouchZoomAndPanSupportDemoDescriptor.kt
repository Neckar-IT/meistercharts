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

import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandlerBroker
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import it.neckar.open.collections.fastForEachIndexed
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.TouchCancelEvent
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent
import com.meistercharts.events.gesture.CanvasTouchZoomAndPanSupport

class CanvasTouchZoomAndPanSupportDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Touch Zoom and Pan Support"

  //language=HTML
  override val description: String = """<h3>Demo for Touch zooming and panning</h3>
  """.trimMargin()

  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        //Disable the default zoom and translation layer
        disableZoomAndTranslation()

        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          val lastZoomXChanges = mutableListOf<Double>()

          //Visualizes all mouse events that "come" through
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val canvasTouchZoomAndPanSupport = CanvasTouchZoomAndPanSupport().also {
              it.addHandler(object : CanvasTouchZoomAndPanSupport.Handler {
                override fun translate(oldCenter: Coordinates, newCenter: Coordinates, deltaCenter: Distance): EventConsumption {
                  val zoomAndTranslationSupport = chartSupport.zoomAndTranslationSupport
                  zoomAndTranslationSupport.moveWindow(deltaCenter)
                  return EventConsumption.Consumed
                }

                override fun doubleTap(tapLocation: Coordinates): EventConsumption {
                  chartSupport.zoomAndTranslationSupport.resetToDefaults()
                  return EventConsumption.Ignored
                }

                override fun zoomChange(oldCenter: Coordinates, newCenter: Coordinates, oldDistanceBetweenTouches: Distance, newDistanceBetweenTouches: Distance, zoomFactorChangeX: Double, zoomFactorChangeY: Double): EventConsumption {
                  lastZoomXChanges.add(0, zoomFactorChangeX)
                  while (lastZoomXChanges.size > 20) {
                    lastZoomXChanges.removeLast()
                  }

                  val zoomAndTranslationSupport = chartSupport.zoomAndTranslationSupport
                  zoomAndTranslationSupport.modifyZoom(true, AxisSelection.Both, newCenter, zoomFactorChangeX, zoomFactorChangeY)
                  return EventConsumption.Consumed
                }
              })
            }

            override val touchEventHandler: CanvasTouchEventHandler = CanvasTouchEventHandlerBroker().also {
              it.delegate(object : CanvasTouchEventHandler {
                override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
                  this@ChartingDemo.markAsDirty()
                  return EventConsumption.Ignored
                }

                override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
                  this@ChartingDemo.markAsDirty()
                  return EventConsumption.Ignored
                }

                override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
                  this@ChartingDemo.markAsDirty()
                  return EventConsumption.Ignored
                }

                override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
                  this@ChartingDemo.markAsDirty()
                  return EventConsumption.Ignored
                }
              })

              it.delegate(canvasTouchZoomAndPanSupport.connectedTouchEventHandler())
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartCalculator = paintingContext.chartCalculator

              canvasTouchZoomAndPanSupport.center?.let { center ->
                //Paint the "dead zone"
                gc.fill(Color.silver)
                val minDistance = canvasTouchZoomAndPanSupport.minDistanceBetweenTouches
                gc.fillRect(0.0, center.y - minDistance / 2.0, gc.width, minDistance) //horizontal
                gc.fillRect(center.x - minDistance / 2.0, 0.0, minDistance, gc.height) //vertical

                //Paint the center point itself
                gc.paintMark(center, 5.0, Color.orange)
              }

              canvasTouchZoomAndPanSupport.touchCoordinates.forEach {
                gc.paintMark(it.value, 5.0, Color.gray)
              }

              gc.fill(Color.black)
              lastZoomXChanges.fastForEachIndexed { index, value ->
                gc.fillText("$value", 5.0, 5.0 + index * 12.0, Direction.TopLeft)
              }

              //if (lastEvents.isNotEmpty()) {
              //  gc.translateToCenter()
              //  gc.fillText("Other mouse events:", 0.0, 0.0, Direction.BottomLeft, 10.0)
              //
              //  lastEvents.fastForEach {
              //    gc.translate(0.0, 20.0)
              //    gc.fillText(it.toString(), 0.0, 0.0, Direction.BottomLeft, 10.0)
              //  }
              //}
            }
          })

          //configurableSizeSeparate("Size", myLayer::size) {
          //  min = -200.0
          //  max = 200.0
          //}
        }
      }
    }
  }
}

