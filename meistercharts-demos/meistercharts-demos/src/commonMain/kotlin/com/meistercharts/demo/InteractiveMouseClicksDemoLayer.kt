package com.meistercharts.demo

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.fillRect
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import it.neckar.open.collections.EvictingQueue
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseClickEvent
import it.neckar.open.unit.other.px

/**
 * Visualizes clicks
 */
class InteractiveMouseClicksDemoLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  val clickedPoints: EvictingQueue<Coordinates> = EvictingQueue(30)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    //Outer line
    gc.lineWidth = 2.0
    gc.strokeStyle(Color.red)
    strokeRect(0.0, gc)

    //5px inner line
    gc.lineWidth = 1.0
    gc.strokeStyle(Color.green)
    strokeRect(5.5, gc)

    gc.lineWidth = 1.0
    gc.strokeStyle(Color.brown)
    strokeRect(10.5, gc)

    gc.lineWidth = 1.0
    gc.strokeStyle(Color.blue)
    strokeRect(20.5, gc)


    //Paint the locations of the interactions
    clickedPoints.forEach {
      gc.fillStyle(Color.darkgrey)
      gc.fillRect(it.x, it.y, 15.0, 15.0, Direction.Center)
    }
  }

  private fun strokeRect(@px inAbsolute: Double, gc: CanvasRenderingContext) {
    gc.strokeRect(inAbsolute, inAbsolute, gc.width - inAbsolute * 2, gc.height - inAbsolute * 2)
  }

  override val mouseEventHandler: CanvasMouseEventHandler? = object : CanvasMouseEventHandler {
    override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
      clickedPoints.add(event.coordinates)

      //notify that a repaint is necessary
      chartSupport.markAsDirty()
      return EventConsumption.Ignored
    }
  }
}
