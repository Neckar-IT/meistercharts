package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.debug
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.KeyCode
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.ModifierCombination
import it.neckar.open.kotlin.lang.toggle

/**
 * A layer that toggles the debugging mode
 */
class ToggleDebuggingModeLayer : AbstractLayer() {
  val data: Data = Data()

  /**
   * Move to background to ensure this layer is the last layer that receives the key events
   */
  override val type: LayerType = LayerType.Background

  override fun paint(paintingContext: LayerPaintingContext) {
  }

  override val keyEventHandler: CanvasKeyEventHandler = object : CanvasKeyEventHandler {
    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      if (event.keyStroke == data.toggleDebugKeyStroke) {
        chartSupport.debug.toggle()
        chartSupport.layerSupport.markAsDirty()

        return EventConsumption.Consumed
      }
      if (event.keyStroke == data.toggleRecordPaintStatisticsKeyStroke) {
        chartSupport.layerSupport::recordPaintStatistics.toggle()
        chartSupport.layerSupport.markAsDirty()

        return EventConsumption.Consumed
      }

      return EventConsumption.Ignored
    }
  }

  class Data {
    val toggleDebugKeyStroke: KeyStroke = KeyStroke(KeyCode('D'), ModifierCombination.CtrlShiftAlt)
    val toggleRecordPaintStatisticsKeyStroke: KeyStroke = KeyStroke(KeyCode('P'), ModifierCombination.CtrlShiftAlt)
  }
}
