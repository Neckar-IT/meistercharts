package com.meistercharts.events

import com.meistercharts.events.EventConsumption

/**
 *  Listener for [TouchEvent]s
 *
 *  Implementations *consume* events
 *
 */
interface TouchEventHandler {
  /**
   * Is notified when a [TouchStartEvent] is fired
   */
  fun onStart(event: TouchStartEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [TouchEndEvent] is fired
   */
  fun onEnd(event: TouchEndEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [TouchMoveEvent] is fired
   */
  fun onMove(event: TouchMoveEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [TouchCancelEvent] is fired
   */
  fun onCancel(event: TouchCancelEvent): EventConsumption {
    return EventConsumption.Ignored
  }
}
