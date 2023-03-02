package com.meistercharts.algorithms.layers

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.EventConsumption.Ignored
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.matches

/**
 * layer that toggles the visibility on shortcut (Ctrl-Alt-V)
 *
 */
class LayerVisibilityToggleOnShortcut(
  val delegate: LayerVisibilityAdapterWithState,
  val keyStroke: KeyStroke
) : Layer by delegate {

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    delegate.layout(paintingContext)
  }

  override val keyEventHandler: CanvasKeyEventHandler? = object : CanvasKeyEventHandler {
    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      if (!keyStroke.matches(event)) {
        return Ignored
      }

      delegate.toggleVisibility()
      chartSupport.markAsDirty()
      return Consumed
    }
  }
}

/**
 * Wraps the layer into an [LayerVisibilityAdapter]
 */
fun LayerVisibilityAdapterWithState.toggleShortcut(keyStroke: KeyStroke): LayerVisibilityToggleOnShortcut {
  return LayerVisibilityToggleOnShortcut(this, keyStroke)
}
