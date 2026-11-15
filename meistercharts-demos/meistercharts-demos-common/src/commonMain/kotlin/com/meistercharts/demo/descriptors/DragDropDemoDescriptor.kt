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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableSizeSeparate
import com.meistercharts.events.EventConsumption
import it.neckar.events.MouseClickEvent
import it.neckar.events.MouseDoubleClickEvent
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseDragEvent
import it.neckar.events.MouseEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.events.MouseUpEvent
import it.neckar.events.MouseWheelEvent
import com.meistercharts.events.gesture.CanvasDragSupport
import com.meistercharts.events.gesture.connectedMouseEventHandler
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Distance
import it.neckar.geometry.within
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.deleteFromStartUntilMaxSize
import it.neckar.open.unit.si.ms

class DragDropDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("b99456b6-4939-4efd-9806-23afa7f233fe")
  override val name: String = "Drag'n'Drop"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val description: String = "Drag the blue box around; other mouse events are painted on the canvas."

  override val category: DemoCategory = DemoCategory.Interaction

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          //Visualizes all mouse events that "come" through
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
              val maxEventsCount = 15

              override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onClick(event, chartSupport)
              }

              override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onDown(event, chartSupport)
              }

              override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onUp(event, chartSupport)
              }

              override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onDoubleClick(event, chartSupport)
              }

              override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onMove(event, chartSupport)
              }

              override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onDrag(event, chartSupport)
              }

              override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onWheel(event, chartSupport)
              }
            }


            val lastEvents: MutableList<MouseEvent> = mutableListOf()

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              if (lastEvents.isNotEmpty()) {
                gc.translateToCenter()
                gc.fillText("Unconsumed mouse events:", 0.0, 0.0, Direction.BottomLeft, 10.0, 10.0)

                lastEvents.fastForEach {
                  gc.translate(0.0, 20.0)
                  gc.fillText(it.toString(), 0.0, 0.0, Direction.BottomLeft, 10.0, 10.0)
                }
              }
            }
          })

          val myLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val canvasDragSupport = CanvasDragSupport().also {
              it.handle(object : CanvasDragSupport.Handler {
                override fun isDraggingAllowedFromHere(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): Boolean {
                  return location.within(rectangleLocation, size)
                }

                override fun onDrag(source: CanvasDragSupport, @Window location: Coordinates, @Zoomed distance: Distance, @ms deltaTime: Double, chartSupport: ChartSupport): EventConsumption {
                  rectangleLocation = rectangleLocation.plus(distance)
                  markAsDirty()
                  return EventConsumption.Consumed
                }

                override fun onFinish(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): EventConsumption {
                  markAsDirty()
                  return super.onFinish(source, location, chartSupport)
                }
              })
            }

            override val mouseEventHandler: CanvasMouseEventHandler = canvasDragSupport.connectedMouseEventHandler()

            var rectangleLocation: @Window Coordinates = Coordinates.origin

            var size: @Zoomed Size = Size(150.0, 150.0)

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              if (canvasDragSupport.dragging) {
                gc.fill(Color.red)
              } else {
                gc.fill(Color.blue)
              }

              gc.fillRect(rectangleLocation, size)
            }
          }

          layers.addLayer(
            myLayer
          )

          configurableSeparate("Size", myLayer::size) {
            min = -200.0
            max = 200.0
          }
        }
      }
    }
  }
}

