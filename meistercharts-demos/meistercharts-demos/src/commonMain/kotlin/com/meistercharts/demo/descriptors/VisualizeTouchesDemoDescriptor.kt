package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.design.Theme
import com.meistercharts.model.Direction
import it.neckar.open.collections.fastForEach
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.Touch
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchEvent
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent

/**
 *
 */
class VisualizeTouchesDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Visualizes all touches"

  override val description: String = """
  """.trimIndent()

  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          this.layers.addClearBackground()

          this.layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            /**
             * Contains the target touches of the last event
             */
            val lastTargetTouches = mutableListOf<Touch>()
            val lastChangedTouches = mutableSetOf<Touch>()

            /**
             * The total number of touches
             */
            var lastTouchesCount = 0

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.fill(Color.black)
              gc.fillText("Total Touch Count: $lastTouchesCount", 10.0, 10.0, Direction.TopLeft)
              gc.fillText("Target Touch Count: ${lastTargetTouches.size}", 10.0, 30.0, Direction.TopLeft)

              lastTargetTouches.fastForEach {
                val color = Theme.chartColors().valueAt(it.touchId.id)
                val location = it.coordinates

                //Paint round border for changed events
                if (lastChangedTouches.contains(it)) {
                  gc.fill(Color.red)
                  gc.fillOvalCenter(location, 80.0)
                }

                gc.fill(color)
                gc.fillOvalCenter(location, 50.0)


                gc.fill(Color.white)
                gc.font(FontDescriptorFragment.XL)
                gc.fillText(it.touchId.id.toString(), it.coordinates, Direction.Center)
              }
            }

            override val touchEventHandler: CanvasTouchEventHandler = object : CanvasTouchEventHandler {
              private fun updateTouches(event: TouchEvent) {
                lastTargetTouches.clear()
                lastTargetTouches.addAll(event.targetTouches)
                lastTouchesCount = event.touches.size
                lastChangedTouches.clear()
                lastChangedTouches.addAll(event.changedTouches)
                markAsDirty()
              }

              override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
                updateTouches(event)
                return EventConsumption.Consumed
              }

              override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
                updateTouches(event)
                return EventConsumption.Consumed
              }

              override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
                updateTouches(event)
                return EventConsumption.Consumed
              }
            }
          })
        }
      }
    }
  }
}
