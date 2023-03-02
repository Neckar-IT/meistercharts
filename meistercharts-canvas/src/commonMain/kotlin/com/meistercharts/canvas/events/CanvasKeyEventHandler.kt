package com.meistercharts.canvas.events

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyTypeEvent
import com.meistercharts.events.KeyUpEvent

/**
 * Event handler for key events from a canvas
 */
interface CanvasKeyEventHandler {
  /**
   * Is notified when a key has been pressed
   */
  fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a key has been released
   */
  fun onUp(event: KeyUpEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a key has been typed
   */
  fun onType(event: KeyTypeEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }
}
