package com.meistercharts.events

import com.meistercharts.events.EventConsumption
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyTypeEvent
import com.meistercharts.events.KeyUpEvent

/**
 * Offers a way to register listeners for key events.
 *
 * Instances *provide* events
 */
interface KeyEventBroker {
  /**
   * Registers a lambda that is notified when a key has been pressed
   */
  fun onDown(handler: (KeyDownEvent) -> EventConsumption)

  /**
   * Registers a lambda that is notified when a key has been released
   */
  fun onUp(handler: (KeyUpEvent) -> EventConsumption)

  /**
   * Registers a lambda that is notified when a key has been typed
   */
  fun onType(handler: (KeyTypeEvent) -> EventConsumption)
}
