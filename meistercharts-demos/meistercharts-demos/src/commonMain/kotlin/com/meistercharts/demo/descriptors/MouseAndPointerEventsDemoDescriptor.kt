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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Arrows
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasPointerEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.strokeCross
import com.meistercharts.canvas.strokeCross45Degrees
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.deleteFromStartUntilMaxSize
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.PointerDownEvent
import com.meistercharts.events.PointerMoveEvent
import com.meistercharts.events.PointerOverEvent
import com.meistercharts.events.PointerUpEvent
import com.meistercharts.events.TouchCancelEvent
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent

/**
 *
 */
class MouseAndPointerEventsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Mouse and Pointer events"
  override val category: DemoCategory = DemoCategory.Interaction

  private val upArrow = Arrows.to(Direction.TopCenter, 7.0)
  private val downArrow = Arrows.to(Direction.BottomCenter, 7.0)

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Notification

            var showPointerEvents = true
            var showMouseEvents = true
            var showTouchEvents = true

            val mouseClickLocations: MutableList<Coordinates> = mutableListOf()
            val mouseDownLocations: MutableList<Coordinates> = mutableListOf()
            val mouseUpLocations: MutableList<Coordinates> = mutableListOf()
            val mouseMoveLocations: MutableList<Coordinates> = mutableListOf()
            val mouseDragLocations: MutableList<Coordinates> = mutableListOf()

            override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
              override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
                mouseClickLocations.add(event.coordinates)
                mouseClickLocations.deleteFromStartUntilMaxSize(100)
                this@ChartingDemo.markAsDirty()
                return EventConsumption.Consumed
              }

              override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
                mouseDownLocations.add(event.coordinates)
                mouseDownLocations.deleteFromStartUntilMaxSize(100)
                this@ChartingDemo.markAsDirty()
                return EventConsumption.Consumed
              }

              override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
                mouseUpLocations.add(event.coordinates)
                mouseUpLocations.deleteFromStartUntilMaxSize(100)
                this@ChartingDemo.markAsDirty()
                return EventConsumption.Consumed
              }

              override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
                event.coordinates?.let {
                  mouseMoveLocations.add(it)
                  mouseMoveLocations.deleteFromStartUntilMaxSize(1000)
                  this@ChartingDemo.markAsDirty()
                }
                return EventConsumption.Consumed
              }

              override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
                event.coordinates.let {
                  mouseDragLocations.add(it)
                  mouseDragLocations.deleteFromStartUntilMaxSize(1000)
                  this@ChartingDemo.markAsDirty()
                }
                return EventConsumption.Consumed
              }
            }

            var pointerDownLocations: MutableList<Coordinates> = mutableListOf()
            var pointerUpLocations: MutableList<Coordinates> = mutableListOf()
            var pointerMoveLocations: MutableList<Coordinates> = mutableListOf()
            var pointerOverLocations: MutableList<Coordinates> = mutableListOf()

            override val pointerEventHandler: CanvasPointerEventHandler = object : CanvasPointerEventHandler {
              override fun onDown(event: PointerDownEvent, chartSupport: ChartSupport): EventConsumption {
                pointerDownLocations.add(event.coordinates)
                this@ChartingDemo.markAsDirty()
                return EventConsumption.Ignored
              }

              override fun onUp(event: PointerUpEvent, chartSupport: ChartSupport): EventConsumption {
                pointerUpLocations.add(event.coordinates)
                this@ChartingDemo.markAsDirty()
                return EventConsumption.Ignored
              }

              override fun onMove(event: PointerMoveEvent, chartSupport: ChartSupport): EventConsumption {
                pointerMoveLocations.add(event.coordinates)
                this@ChartingDemo.markAsDirty()
                return EventConsumption.Ignored
              }

              override fun onOver(event: PointerOverEvent, chartSupport: ChartSupport): EventConsumption {
                pointerOverLocations.add(event.coordinates)
                this@ChartingDemo.markAsDirty()
                return EventConsumption.Ignored
              }
            }

            var touchStartLocations: MutableList<Coordinates> = mutableListOf()
            var touchEndLocations: MutableList<Coordinates> = mutableListOf()
            var touchMoveLocations: MutableList<Coordinates> = mutableListOf()
            var touchCanceledLocations: MutableList<Coordinates> = mutableListOf()

            override val touchEventHandler: CanvasTouchEventHandler = object : CanvasTouchEventHandler {
              override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
                touchStartLocations.add(event.firstChanged.coordinates)
                this@ChartingDemo.markAsDirty()
                return EventConsumption.Ignored
              }

              override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
                touchMoveLocations.add(event.firstChanged.coordinates)
                this@ChartingDemo.markAsDirty()
                return EventConsumption.Ignored
              }

              override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
                touchEndLocations.add(event.firstChanged.coordinates)
                this@ChartingDemo.markAsDirty()
                return EventConsumption.Ignored
              }

              override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
                touchCanceledLocations.add(event.firstChanged.coordinates)
                this@ChartingDemo.markAsDirty()
                return EventConsumption.Ignored
              }
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc


              //Touch events
              if (showTouchEvents) {

                gc.stroke(Color.green)
                gc.fill(Color.green)


                touchStartLocations.fastForEach { coordinates ->
                  gc.saved {
                    gc.translate(coordinates.x, coordinates.y)
                    gc.stroke(downArrow)
                  }
                }
                touchEndLocations.fastForEach { coordinates ->
                  gc.saved {
                    gc.translate(coordinates.x, coordinates.y)
                    gc.stroke(upArrow)
                  }
                }

                touchMoveLocations.fastForEach { coordinates ->
                  gc.fillOvalCenter(coordinates, Size.of(0.8, 0.3))
                }

                gc.saved {
                  gc.stroke(Color.darkblue)
                  gc.fill(Color.darkblue)

                  touchCanceledLocations.fastForEach { coordinates ->
                    gc.strokeCross(coordinates.x, coordinates.y)
                  }
                }
              }

              //
              //
              //
              //pointer events are painted in blue
              //
              //
              //

              if (showPointerEvents) {

                gc.stroke(Color.blue)
                gc.fill(Color.blue)


                pointerDownLocations.fastForEach { coordinates ->
                  gc.saved {
                    gc.translate(coordinates.x, coordinates.y)
                    gc.stroke(downArrow)
                  }
                }
                pointerUpLocations.fastForEach { coordinates ->
                  gc.saved {
                    gc.translate(coordinates.x, coordinates.y)
                    gc.stroke(upArrow)
                  }
                }

                pointerMoveLocations.fastForEach { coordinates ->
                  gc.fillOvalCenter(coordinates, Size.of(0.8, 0.3))
                }

                gc.saved {
                  gc.stroke(Color.darkblue)
                  gc.fill(Color.darkblue)

                  pointerOverLocations.fastForEach { coordinates ->
                    gc.strokeCross45Degrees(coordinates.x, coordinates.y)
                  }
                }
              }

              if (showMouseEvents) {
                //Mouse events are painted in orange
                gc.stroke(Color.orange)
                gc.fill(Color.orange)

                mouseDownLocations.fastForEach { coordinates ->
                  gc.saved {
                    gc.translate(coordinates.x, coordinates.y)
                    gc.stroke(downArrow)
                  }
                }
                mouseUpLocations.fastForEach { coordinates ->
                  gc.saved {
                    gc.translate(coordinates.x, coordinates.y)
                    gc.stroke(upArrow)
                  }
                }

                mouseMoveLocations.fastForEach { coordinates ->
                  gc.fillOvalCenter(coordinates, Size.of(0.5, 0.5))
                }

                gc.saved {
                  gc.stroke(Color.orangered)
                  gc.fill(Color.orangered)

                  mouseDragLocations.fastForEach { coordinates ->
                    gc.fillOvalCenter(coordinates, Size.of(0.5, 0.5))
                  }
                }

                mouseClickLocations.fastForEach { coordinates ->
                  gc.strokeCross45Degrees(coordinates.x, coordinates.y)
                }
              }

            }
          }
          layers.addLayer(layer)


          configurableBoolean("Show Mouse Events", layer::showMouseEvents)
          configurableBoolean("Show Pointer Events", layer::showPointerEvents)
          configurableBoolean("Show Touch Events", layer::showTouchEvents)
        }
      }
    }
  }
}
