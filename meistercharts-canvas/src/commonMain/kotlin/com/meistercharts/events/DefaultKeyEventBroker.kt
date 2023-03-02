package com.meistercharts.events

import it.neckar.open.kotlin.lang.consumeUntil
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.EventConsumption.Ignored

/**
 * Handles key events. Offers a way to register listeners and call them on key events
 *
 */
class DefaultKeyEventBroker : KeyEventBroker {
  /**
   * Handles key pressed events
   */
  private val downCallbacks: MutableList<(KeyDownEvent) -> EventConsumption> = mutableListOf()

  /**
   * Registers a lambda that is notified when a key has been pressed
   */
  override fun onDown(handler: (KeyDownEvent) -> EventConsumption) {
    downCallbacks.add(handler)
  }

  /**
   * Notifies the callbacks that a key has been pressed
   */
  fun notifyDown(keyEvent: KeyDownEvent): EventConsumption {
    return downCallbacks.consumeUntil(Consumed) {
      it(keyEvent)
    } ?: Ignored
  }

  /**
   * Handles key released events
   */
  private val upCallbacks: MutableList<(KeyUpEvent) -> EventConsumption> = mutableListOf()

  /**
   * Registers a lambda that is notified when a key has been released
   */
  override fun onUp(handler: (KeyUpEvent) -> EventConsumption) {
    upCallbacks.add(handler)
  }

  /**
   * Notifies the callbacks that a key has been released
   */
  fun notifyUp(keyEvent: KeyUpEvent): EventConsumption {
    return upCallbacks.consumeUntil(Consumed) {
      it(keyEvent)
    } ?: Ignored
  }

  /**
   * Handles key typed events
   */
  private val typeCallbacks: MutableList<(KeyTypeEvent) -> EventConsumption> = mutableListOf()

  /**
   * Registers a lambda that is notified when a key has been typed
   */
  override fun onType(handler: (KeyTypeEvent) -> EventConsumption) {
    typeCallbacks.add(handler)
  }

  /**
   * Notifies the callbacks that a key has been typed
   */
  fun notifyTyped(keyEvent: KeyTypeEvent): EventConsumption {
    return typeCallbacks.consumeUntil(Consumed) {
      it(keyEvent)
    } ?: Ignored
  }
}
