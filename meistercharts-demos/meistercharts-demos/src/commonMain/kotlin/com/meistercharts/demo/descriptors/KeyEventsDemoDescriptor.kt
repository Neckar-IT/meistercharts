package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyEvent
import com.meistercharts.events.KeyTypeEvent
import com.meistercharts.events.KeyUpEvent

/**
 *
 */
class KeyEventsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Key events"
  override val category: DemoCategory = DemoCategory.Interaction

  //language=HTML
  override val description: String = """
    Visualizes Key Events

    <h3>Expected Results for Ctrl-Alt-Shift-A</h3>
    Ctrl + Alt + Shift + "A" (65)

    <p>(Tested on all platforms)</p>

  """.trimIndent()

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var lastDownEvent: KeyDownEvent? = null
            var lastUpEvent: KeyUpEvent? = null
            var lastTypedEvent: KeyTypeEvent? = null

            override val keyEventHandler: CanvasKeyEventHandler = object : CanvasKeyEventHandler {
              override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
                lastDownEvent = event
                markAsDirty()
                return EventConsumption.Ignored
              }

              override fun onUp(event: KeyUpEvent, chartSupport: ChartSupport): EventConsumption {
                lastUpEvent = event
                markAsDirty()
                return EventConsumption.Ignored
              }

              override fun onType(event: KeyTypeEvent, chartSupport: ChartSupport): EventConsumption {
                lastTypedEvent = event
                markAsDirty()
                return EventConsumption.Ignored
              }
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(10.0, 10.0)

              gc.saved {
                gc.printHeader()
              }

              gc.translate(00.0, 30.0)

              gc.fillText("Down", 0.0, 0.0, Direction.TopLeft)
              lastDownEvent?.let { event ->
                gc.saved {
                  gc.printDebug(event)
                }
              }

              gc.translate(0.0, 20.0)
              gc.fillText("Up", 0.0, 0.0, Direction.TopLeft)
              lastUpEvent?.let { event ->
                gc.saved {
                  gc.printDebug(event)
                }
              }

              gc.translate(0.0, 20.0)
              gc.fillText("Typed", 0.0, 0.0, Direction.TopLeft)
              lastTypedEvent?.let { event ->
                gc.printDebug(event)
              }
            }
          })
        }

      }
    }
  }
}

private fun CanvasRenderingContext.printHeader() {
  translate(100.0, 0.0)
  fillText("Type", 0.0, 0.0, Direction.TopLeft)

  translate(70.0, 0.0)
  fillText("Relative Time", 0.0, 0.0, Direction.TopLeft)

  translate(200.0, 0.0)
  fillText("Code", 0.0, 0.0, Direction.TopLeft)

  translate(70.0, 0.0)
  fillText("as Char", 0.0, 0.0, Direction.TopLeft)

  translate(70.0, 0.0)
  fillText("Alt", 0.0, 0.0, Direction.TopLeft)

  translate(50.0, 0.0)
  fillText("Ctrl", 0.0, 0.0, Direction.TopLeft)

  translate(50.0, 0.0)
  fillText("Meta", 0.0, 0.0, Direction.TopLeft)

  translate(50.0, 0.0)
  fillText("Shift", 0.0, 0.0, Direction.TopLeft)
}

private fun CanvasRenderingContext.printDebug(keyEvent: KeyEvent) {
  translate(100.0, 0.0)
  fillText(keyEvent.text, 0.0, 0.0, Direction.TopLeft)

  translate(70.0, 0.0)
  fillText(keyEvent.timestamp.toString(), 0.0, 0.0, Direction.TopLeft)

  translate(200.0, 0.0)
  val keyStroke = keyEvent.keyStroke
  fillText(keyStroke.keyCode.code.toString(), 0.0, 0.0, Direction.TopLeft)

  translate(70.0, 0.0)
  fillText(keyStroke.keyCode.code.toChar().toString(), 0.0, 0.0, Direction.TopLeft)

  translate(70.0, 0.0)
  fillText(keyStroke.modifierCombination.alt.toCharRepresentation(), 0.0, 0.0, Direction.TopLeft)

  translate(50.0, 0.0)
  fillText(keyStroke.modifierCombination.control.toCharRepresentation(), 0.0, 0.0, Direction.TopLeft)

  translate(50.0, 0.0)
  fillText(keyStroke.modifierCombination.meta.toCharRepresentation(), 0.0, 0.0, Direction.TopLeft)

  translate(50.0, 0.0)
  fillText(keyStroke.modifierCombination.shift.toCharRepresentation(), 0.0, 0.0, Direction.TopLeft)
}

private fun Boolean.toCharRepresentation(): String {
  return if (this) "+" else "-"
}
