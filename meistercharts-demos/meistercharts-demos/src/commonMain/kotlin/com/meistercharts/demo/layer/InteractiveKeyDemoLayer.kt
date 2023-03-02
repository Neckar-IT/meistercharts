package com.meistercharts.layer

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.text.TextPainter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.LineSpacing
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.model.Direction
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.KeyCode
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyTypeEvent
import com.meistercharts.events.KeyUpEvent
import com.meistercharts.style.BoxStyle
import it.neckar.logging.LoggerFactory

/**
 * Demo layer that shows how to process/handle key presses
 */
class InteractiveKeyDemoLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  private var lastText: String? = null
  private var lastKeyCode: KeyCode? = null

  override fun paint(paintingContext: LayerPaintingContext) {
    val text = "keyCode: $lastKeyCode \n text: $lastText"
    paintingContext.gc.translateToCenter()
    TextPainter().paintText(
      gc = paintingContext.gc,
      lines = listOf(text),
      textColor = Color.red,
      boxStyle = BoxStyle.none,
      lineSpacing = LineSpacing.Single,
      horizontalAlignment = HorizontalAlignment.Left,
      anchorDirection = Direction.TopLeft,
      anchorGapHorizontal = 0.0,
      anchorGapVertical = 0.0,
      maxStringWidth = 50.0
    )
  }

  override val keyEventHandler: CanvasKeyEventHandler? = object : CanvasKeyEventHandler {
    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      logger.debug("Key pressed: $event")
      lastKeyCode = event.keyStroke.keyCode
      lastText = event.text

      chartSupport.markAsDirty()
      return EventConsumption.Ignored
    }

    override fun onUp(event: KeyUpEvent, chartSupport: ChartSupport): EventConsumption {
      logger.debug("Key released: $event")
      chartSupport.markAsDirty()
      return EventConsumption.Ignored
    }

    override fun onType(event: KeyTypeEvent, chartSupport: ChartSupport): EventConsumption {
      logger.debug("Key typed: $event")
      chartSupport.markAsDirty()
      return EventConsumption.Ignored
    }
  }

  companion object {
    val logger = LoggerFactory.getLogger("com.meistercharts.layer.InteractiveKeyDemoLayer")
  }
}
