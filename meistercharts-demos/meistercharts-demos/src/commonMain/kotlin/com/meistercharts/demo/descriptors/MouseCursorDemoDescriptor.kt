package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.mouseCursorSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.design.Theme
import com.meistercharts.model.Direction
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.kotlin.lang.toIntFloor
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseMoveEvent

/**
 *
 */
class MouseCursorDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Mouse Cursor"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          //All available mouse cursors
          val mouseCursors = MouseCursor.values()


          val myLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val size = 35.0

            override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
              override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
                val mouseCursorSupport = chartSupport.mouseCursorSupport

                mouseCursorSupport.cursorProperty("MyLayer").value = event.coordinates?.let {
                  val y = it.y - 10.0

                  val index = (y / (size + 5)).toIntFloor()
                  mouseCursors.getOrNull(index)
                }

                return EventConsumption.Consumed
              }
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translate(10.0, 10.0)

              mouseCursors.fastForEachIndexed { index, mouseCursor ->
                gc.fill(Theme.chartColors().valueAt(index))
                gc.fillRect(0.0, 0.0, gc.width - 20.0, size)
                gc.fill(Color.white)
                gc.fillText(mouseCursor.name, 0.0, size / 2.0, Direction.CenterLeft, 5.0, 5.0)
                gc.translate(0.0, size + 5.0)
              }

              paintingContext.chartSupport.mouseCursorSupport
            }
          }

          layers.addLayer(myLayer)
        }

        //layerSupport.chartSupport.canvas.mouseCursor.toJavaFx(), MouseCursor.values()
      }
    }
  }
}
