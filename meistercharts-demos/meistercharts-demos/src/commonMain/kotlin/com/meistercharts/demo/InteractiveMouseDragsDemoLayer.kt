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
package com.meistercharts.demo

import it.neckar.open.collections.EvictingQueue
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.gesture.CanvasDragSupport
import com.meistercharts.events.gesture.Speed
import com.meistercharts.events.gesture.connectedMouseEventHandler
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.Path
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance

/**
 * Visualizes drag events
 */
class InteractiveMouseDragsDemoLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  /**
   * Contains all positions that have been dragged
   */
  private val dragged: EvictingQueue<DragInfo> = EvictingQueue(300)

  private val dragSupport = CanvasDragSupport()

  init {
    dragSupport.handle(object : CanvasDragSupport.Handler {
      override fun isDraggingAllowedFromHere(canvasDragSupport: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): Boolean {
        dragged.clear()
        return true
      }

      override fun onDrag(source: CanvasDragSupport, location: Coordinates, distance: Distance, deltaTime: Double, chartSupport: ChartSupport): EventConsumption {
        val speed = dragSupport.dragSpeedCalculator.calculateSpeed()
        dragged.add(DragInfo(location, distance, speed))
        return EventConsumption.Consumed
      }
    })
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    //Paint only the coordinates
    gc.lineWidth = 4.0
    gc.strokeStyle(Color.orange)
    gc.stroke(Path.from(dragged.map { it.coordinates }))

    //Paint using the provided distance
    gc.lineWidth = 0.5
    gc.strokeStyle(Color.black)
    dragged.forEach {
      gc.strokeLine(it.coordinates.x, it.coordinates.y, it.coordinates.x - it.distance.x, it.coordinates.y - it.distance.y)
    }

    dragged.asSequence()
      .windowed(1, 30, false)
      .forEach {
        val first = it.first()

        gc.fill(Color.cyan)
        gc.fillText("Speed: ${first.speed} ", first.coordinates.x, first.coordinates.y, Direction.TopLeft)
      }

  }

  override val mouseEventHandler: CanvasMouseEventHandler? = dragSupport.connectedMouseEventHandler()
}

data class DragInfo(
  val coordinates: Coordinates,
  val distance: Distance,
  val speed: Speed,
)
