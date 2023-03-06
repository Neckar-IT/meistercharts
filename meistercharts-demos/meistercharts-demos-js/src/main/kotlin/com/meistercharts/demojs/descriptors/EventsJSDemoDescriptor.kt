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
package com.meistercharts.demojs.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Arrows
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.strokeCross
import com.meistercharts.canvas.strokeCross45Degrees
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.js.CanvasJS
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.deleteFromStartUntilMaxSize
import offset
import org.w3c.dom.events.MouseEvent

/**
 * This demo prints the native mouse events on the console
 */
class EventsJSDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Native mouse events"

  //language=HTML
  override val description: String = """
    <h3>Visualizes the native browser events</h3>

    <h4>Up/down events: Red arrows</h4>
    <h4>Click events: Red circle</h4>
    <h4>Move event locations: Silver crosses</h4>
    <h4>Wheel event locations: Orange crosses</h4>
  """.trimIndent()
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    val visualizeMouseCoordsLayer = VisualizeMouseCoordsLayer()

    return ChartingDemo {
      meistercharts {
        configure {
          val canvasElement = (chartSupport.canvas as CanvasJS).canvasElement

          // Listen to the native elements and log them. We must use the capture phase because
          // the CanvasJS adds its own listeners which consume the events in the bubbling phase
          // before the listeners defined here would be notified.

          canvasElement.addEventListener("click", { event ->
            val offset = event.unsafeCast<MouseEvent>().offset()
            println("Mouse clicked @ $offset")
            visualizeMouseCoordsLayer.onClick(offset, chartSupport)
          }, true)

          canvasElement.addEventListener("mousedown", { event ->
            val offset = event.unsafeCast<MouseEvent>().offset()
            println("Mouse down @ $offset")
            visualizeMouseCoordsLayer.onDown(offset, chartSupport)
          }, true)

          canvasElement.addEventListener("mouseup", { event ->
            val offset = event.unsafeCast<MouseEvent>().offset()
            println("Mouse up @ $offset")
            visualizeMouseCoordsLayer.onUp(offset, chartSupport)
          }, true)

          canvasElement.addEventListener("mousemove", { event ->
            val offset = event.unsafeCast<MouseEvent>().offset()
            println("Mouse move @ $offset")
            visualizeMouseCoordsLayer.onMove(offset, chartSupport)
          }, true)

          canvasElement.addEventListener("wheel", { event ->
            val offset = event.unsafeCast<MouseEvent>().offset()
            println("Mouse wheel @ $offset")
            visualizeMouseCoordsLayer.onWheel(offset, chartSupport)
          }, true)


          layers.addClearBackground()
          layers.addTextUnresolved("Printing mouse events on the console", Color.darkorange)
          layers.addLayer(visualizeMouseCoordsLayer)
        }
      }
    }
  }
}

private class VisualizeMouseCoordsLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  private val moveCoordinates = mutableListOf<Coordinates>()
  private val downCoordinates = mutableListOf<Coordinates>()
  private val upCoordinates = mutableListOf<Coordinates>()
  private val clickCoordinates = mutableListOf<Coordinates>()
  private val wheelCoordinates = mutableListOf<Coordinates>()

  fun onMove(coordinates: Coordinates, chartSupport: ChartSupport) {
    handleMouseEvent(chartSupport, coordinates, moveCoordinates)
  }

  fun onDown(coordinates: Coordinates, chartSupport: ChartSupport) {
    handleMouseEvent(chartSupport, coordinates, downCoordinates)
  }

  fun onUp(coordinates: Coordinates, chartSupport: ChartSupport) {
    handleMouseEvent(chartSupport, coordinates, upCoordinates)
  }

  fun onClick(coordinates: Coordinates, chartSupport: ChartSupport) {
    handleMouseEvent(chartSupport, coordinates, clickCoordinates)
  }

  fun onWheel(coordinates: Coordinates, chartSupport: ChartSupport) {
    handleMouseEvent(chartSupport, coordinates, wheelCoordinates)
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
