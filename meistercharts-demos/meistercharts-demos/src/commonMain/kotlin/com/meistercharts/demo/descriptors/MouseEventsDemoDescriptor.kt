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
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Arrows
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.strokeCross
import com.meistercharts.canvas.strokeCross45Degrees
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.deleteFromStartUntilMaxSize
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.MouseWheelEvent
import it.neckar.logging.LoggerFactory

/**
 * This demo prints the mouse events on the console
 */
class MouseEventsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Mouse Events"

  //language=HTML
  override val description: String = """
    <h3>Visualizes the mouse events</h3>

    <h4>Up/down events: Red arrows</h4>
    <h4>Click events: Red circle</h4>
    <h4>Move event locations: Silver crosses</h4>
    <h4>Wheel event locations: Orange crosses</h4>
  """.trimIndent()
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addTextUnresolved("Printing mouse events on the console", Color.darkorange)
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
            }

            override val mouseEventHandler: CanvasMouseEventHandler? = object : CanvasMouseEventHandler {
              override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
                super.onClick(event, chartSupport)
                logger.debug("Mouse clicked @ ${event.coordinates}")
                return EventConsumption.Ignored
              }

              override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
                super.onDoubleClick(event, chartSupport)
                logger.debug("Mouse double clicked @ ${event.coordinates}")
                return EventConsumption.Ignored
              }

              override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
                super.onMove(event, chartSupport)
                logger.debug("Mouse moved @ ${event.coordinates}")
                return EventConsumption.Ignored
              }

              override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
                super.onDrag(event, chartSupport)
                logger.debug("Mouse dragged @ ${event.coordinates}")
                return EventConsumption.Ignored
              }

              override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
                super.onWheel(event, chartSupport)
                logger.debug("Mouse wheel @ ${event.coordinates} - Delta: ${event.delta}")
                return EventConsumption.Ignored
              }

              override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
                super.onDown(event, chartSupport)
                logger.debug("Mouse down @ ${event.coordinates}")
                return EventConsumption.Ignored
              }

              override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
                super.onUp(event, chartSupport)
                logger.debug("Mouse up @ ${event.coordinates}")
                return EventConsumption.Ignored
              }
            }
          })

          layers.addLayer(VisualizeMouseMoveCoordsLayer())
        }
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.MouseEventsDemoDescriptor")
  }
}

private class VisualizeMouseMoveCoordsLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  private val moveCoordinates = mutableListOf<Coordinates>()
  private val downCoordinates = mutableListOf<Coordinates>()
  private val upCoordinates = mutableListOf<Coordinates>()
  private val clickCoordinates = mutableListOf<Coordinates>()
  private val wheelCoordinates = mutableListOf<Coordinates>()

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      handleMouseEvent(chartSupport, event.coordinates, moveCoordinates)
      return EventConsumption.Ignored
    }

    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      handleMouseEvent(chartSupport, event.coordinates, downCoordinates)
      return EventConsumption.Ignored
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      handleMouseEvent(chartSupport, event.coordinates, upCoordinates)
      return EventConsumption.Ignored
    }

    override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
      handleMouseEvent(chartSupport, event.coordinates, clickCoordinates)
      return EventConsumption.Ignored
    }

    override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
      handleMouseEvent(chartSupport, event.coordinates, wheelCoordinates)
      return EventConsumption.Ignored
    }
  }

  private fun handleMouseEvent(chartSupport: ChartSupport, coordinates: Coordinates?, eventCoordsList: MutableList<Coordinates>) {
    coordinates?.let {
      eventCoordsList.add(it)
    }

    eventCoordsList.deleteFromStartUntilMaxSize(100)
    chartSupport.markAsDirty()
  }

  private val upArrow = Arrows.to(Direction.TopCenter, 7.0)
  private val downArrow = Arrows.to(Direction.BottomCenter, 7.0)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    gc.stroke(Color.silver)
    moveCoordinates.fastForEach {
      gc.strokeCross(it)
    }

    gc.stroke(Color.orange)
    wheelCoordinates.fastForEach {
      gc.strokeCross45Degrees(it.x, it.y)
    }

    gc.stroke(Color.red)
    downCoordinates.fastForEach { coordinates ->
      gc.saved {
        gc.translate(coordinates.x, coordinates.y)
        gc.stroke(downArrow)
      }
    }

    gc.stroke(Color.red)
    upCoordinates.fastForEach { coordinates ->
      gc.saved {
        gc.translate(coordinates.x, coordinates.y)
        gc.stroke(upArrow)
      }
    }

    gc.stroke(Color.red)
    clickCoordinates.fastForEach {
      gc.strokeOvalCenter(it, Size.PX_24)
    }
  }
}
