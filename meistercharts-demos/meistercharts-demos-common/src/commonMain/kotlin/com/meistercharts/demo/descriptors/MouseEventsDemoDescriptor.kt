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
@file:Suppress("RETURN_VALUE_NOT_USED") // Demo-scope subscriptions tied to the demo lifetime

package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTextUnresolvedColored
import com.meistercharts.algorithms.painter.Arrows
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.canvas.strokeCross
import com.meistercharts.canvas.strokeCross45Degrees
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.events.EventConsumption
import it.neckar.events.MouseClickEvent
import it.neckar.events.MouseDoubleClickEvent
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseDragEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.events.MouseUpEvent
import it.neckar.events.MouseWheelEvent
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import it.neckar.logging.LoggerFactory
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.deleteFromStartUntilMaxSize

/**
 * This demo prints the mouse events on the console
 */
class MouseEventsDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("36ad0723-0654-455b-b6ae-20258b87a5ff")
  override val name: String = "Mouse Events"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override val description: String = "Visualizes mouse events: up/down, click, move, drag and wheel"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        disableZoomAndTranslation()

        configure {
          layers.addClearBackground()
          layers.addTextUnresolvedColored("Printing mouse events on the console", Color.darkorange)
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
            }

            override val mouseEventHandler: CanvasMouseEventHandler? = object : CanvasMouseEventHandler {
              override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
                super.onClick(event, chartSupport)
                logger.info("Mouse clicked @ ${event.coordinates} with button ${event.button}")
                return EventConsumption.Ignored
              }

              override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
                super.onDoubleClick(event, chartSupport)
                logger.info("Mouse double clicked @ ${event.coordinates} with button ${event.button}")
                return EventConsumption.Ignored
              }

              override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
                super.onMove(event, chartSupport)
                logger.info("Mouse moved @ ${event.coordinates}")
                return EventConsumption.Ignored
              }

              override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
                super.onDrag(event, chartSupport)
                logger.info("Mouse dragged @ ${event.coordinates} with button ${event.button}")
                return EventConsumption.Ignored
              }

              override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
                super.onWheel(event, chartSupport)
                logger.info("Mouse wheel @ ${event.coordinates} - Delta: ${event.delta}")
                return EventConsumption.Ignored
              }

              override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
                super.onDown(event, chartSupport)
                logger.info("Mouse down @ ${event.coordinates} with button ${event.button}")
                return EventConsumption.Ignored
              }

              override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
                super.onUp(event, chartSupport)
                logger.info("Mouse up @ ${event.coordinates} with button ${event.button}")
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
  private val dragCoordinates = mutableListOf<Coordinates>()
  private val downCoordinates = mutableListOf<Coordinates>()
  private val upCoordinates = mutableListOf<Coordinates>()
  private val clickCoordinates = mutableListOf<Coordinates>()
  private val wheelCoordinates = mutableListOf<Coordinates>()

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      handleMouseEvent(chartSupport, event.coordinates, moveCoordinates)
      return EventConsumption.Ignored
    }

    override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
      handleMouseEvent(chartSupport, event.coordinates, dragCoordinates)
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
    chartSupport.markAsDirty(DirtyReason.UserInteraction)
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

    gc.stroke(Color.green)
    dragCoordinates.fastForEach {
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
