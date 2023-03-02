package com.meistercharts.events

import com.meistercharts.events.TouchCancelEvent
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchEventBroker
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent
import it.neckar.open.kotlin.lang.consumeUntil

/**
 * Handles [TouchEvent]s
 *
 * Offers a way to register listeners and calls them on [TouchEvent]s
 *
 */
class DefaultTouchEventBroker : TouchEventBroker {

  private val touchStartCallbacks: MutableList<(TouchStartEvent) -> EventConsumption> = mutableListOf()
  private val touchEndCallbacks: MutableList<(TouchEndEvent) -> EventConsumption> = mutableListOf()
  private val touchMoveCallbacks: MutableList<(TouchMoveEvent) -> EventConsumption> = mutableListOf()
  private val touchCancelCallbacks: MutableList<(TouchCancelEvent) -> EventConsumption> = mutableListOf()

  override fun onStart(handler: (TouchStartEvent) -> EventConsumption) {
    touchStartCallbacks.add(handler)
  }

  override fun onEnd(handler: (TouchEndEvent) -> EventConsumption) {
    touchEndCallbacks.add(handler)
  }

  override fun onMove(handler: (TouchMoveEvent) -> EventConsumption) {
    touchMoveCallbacks.add(handler)
  }

  override fun onCancel(handler: (TouchCancelEvent) -> EventConsumption) {
    touchCancelCallbacks.add(handler)
  }

  /**
   * Notify this [TouchEventBroker] about [touchEvent]
   */
  fun notifyOnStart(touchEvent: TouchStartEvent): EventConsumption {
    return touchStartCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(touchEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Notify this [TouchEventBroker] about [touchEvent]
   */
  fun notifyOnEnd(touchEvent: TouchEndEvent): EventConsumption {
    return touchEndCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(touchEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Notify this [TouchEventBroker] about [touchEvent]
   */
  fun notifyOnMove(touchEvent: TouchMoveEvent): EventConsumption {
    return touchMoveCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(touchEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Notify this [TouchEventBroker] about [touchEvent]
   */
  fun notifyOnCancel(touchEvent: TouchCancelEvent): EventConsumption {
    return touchCancelCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(touchEvent)
    } ?: EventConsumption.Ignored
  }
}
