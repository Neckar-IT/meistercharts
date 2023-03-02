package com.meistercharts.events

/**
 * Listens for key events
 *
 * Instances *consume* events
 */
interface KeyEventHandler {
  /**
   * Is notified when a key has been pressed
   */
  fun onDown(event: KeyDownEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a key has been released
   */
  fun onUp(event: KeyUpEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a key has been typed
   */
  fun onType(event: KeyTypeEvent): EventConsumption {
    return EventConsumption.Ignored
  }
}

/**
 * Register the given event handler at the broker
 */
fun KeyEventBroker.register(eventHandler: KeyEventHandler) {
  onDown(eventHandler::onDown)
  onUp(eventHandler::onUp)
  onType(eventHandler::onType)
}
