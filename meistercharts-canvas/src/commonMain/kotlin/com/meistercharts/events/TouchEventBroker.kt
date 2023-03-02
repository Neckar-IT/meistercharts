package com.meistercharts.events

import com.meistercharts.events.EventConsumption

/**
 * Offers a way to register listeners for [TouchEvent]s
 *
 * Instances *provide* events
 *
 */
interface TouchEventBroker {
  /**
   * Registers a callback that is notified when a [TouchStartEvent] is fired
   */
  fun onStart(handler: (TouchStartEvent) -> EventConsumption)

  /**
   * Registers a callback that is notified when a [TouchEndEvent] is fired
   */
  fun onEnd(handler: (TouchEndEvent) -> EventConsumption)

  /**
   * Registers a callback that is notified when a [TouchMoveEvent] is fired
   */
  fun onMove(handler: (TouchMoveEvent) -> EventConsumption)

  /**
   * Registers a callback that is notified when a [TouchCancelEvent] is fired
   */
  fun onCancel(handler: (TouchCancelEvent) -> EventConsumption)
}

/**
 * Register the given event handler at the broker
 */
fun TouchEventBroker.register(eventHandler: TouchEventHandler) {
  onStart(eventHandler::onStart)
  onEnd(eventHandler::onEnd)
  onMove(eventHandler::onMove)
  onCancel(eventHandler::onCancel)
}
