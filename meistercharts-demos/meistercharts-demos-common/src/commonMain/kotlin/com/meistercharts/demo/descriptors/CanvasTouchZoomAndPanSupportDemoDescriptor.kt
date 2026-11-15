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

import com.meistercharts.demo.configurableSeparate
import kotlin.uuid.Uuid
import com.meistercharts.zoom.UpdateReason
import it.neckar.geometry.AxisSelection
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.color.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandlerBroker
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.open.collections.fastForEachIndexed
import com.meistercharts.events.EventConsumption
import it.neckar.events.TouchCancelEvent
import it.neckar.events.TouchEndEvent
import it.neckar.events.TouchMoveEvent
import it.neckar.events.TouchStartEvent
import com.meistercharts.events.gesture.CanvasTouchZoomAndPanSupport

class CanvasTouchZoomAndPanSupportDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("af6d47d4-9d52-45d8-b703-34b59b4ac79a")
  override val name: String = "Touch Zoom and Pan Support"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Demo for touch zooming and panning"

  override val category: DemoCategory = DemoCategory.Interaction

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
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
                  zoomAndTranslationSupport.moveWindow(deltaCenter, reason = UpdateReason.UserInteraction)
                  return EventConsumption.Consumed
                }

                override fun doubleTap(tapLocation: Coordinates): EventConsumption {
                  chartSupport.zoomAndTranslationSupport.resetToDefaults(reason = UpdateReason.UserInteraction)
                  return EventConsumption.Ignored
                }

                override fun zoomChange(oldCenter: Coordinates, newCenter: Coordinates, oldDistanceBetweenTouches: Distance, newDistanceBetweenTouches: Distance, zoomFactorChangeX: Double, zoomFactorChangeY: Double): EventConsumption {
                  lastZoomXChanges.add(0, zoomFactorChangeX)
                  while (lastZoomXChanges.size > 20) {
                    lastZoomXChanges.removeLast()
                  }

                  val zoomAndTranslationSupport = chartSupport.zoomAndTranslationSupport
                  zoomAndTranslationSupport.modifyZoom(true, AxisSelection.Both, newCenter, zoomFactorChangeX, zoomFactorChangeY, reason = UpdateReason.UserInteraction)
                  return EventConsumption.Consumed
                }
              })
            }

            override val touchEventHandler: CanvasTouchEventHandler = CanvasTouchEventHandlerBroker().also {
              it.delegate(object : CanvasTouchEventHandler {
                override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
                  this@MeisterchartsDemo.markAsDirty()
                  return EventConsumption.Ignored
                }

                override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
                  this@MeisterchartsDemo.markAsDirty()
                  return EventConsumption.Ignored
                }

                override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
                  this@MeisterchartsDemo.markAsDirty()
                  return EventConsumption.Ignored
                }

                override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
                  this@MeisterchartsDemo.markAsDirty()
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

                gc.paintMark(center, 5.0, Color.orange())
              }

              canvasTouchZoomAndPanSupport.touchCoordinates.forEach {
                gc.paintMark(it.value, 5.0, Color.gray())
              }

              gc.fill(Color.black)
              lastZoomXChanges.fastForEachIndexed { index, value ->
                gc.fillText("$value", 5.0, 5.0 + index * 12.0, Direction.TopLeft)
              }
            }
          })

        }
      }
    }
  }
}

