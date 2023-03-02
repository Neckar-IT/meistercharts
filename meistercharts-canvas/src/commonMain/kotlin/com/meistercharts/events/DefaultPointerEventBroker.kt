package com.meistercharts.events

import it.neckar.open.kotlin.lang.consumeUntil

/**
 * Handles [PointerEvent]s.
 *
 * Offers a way to register listeners and call them on [PointerEvent]s.
 *
 */
@Deprecated("Use Touch and Mouse events instead")
class DefaultPointerEventBroker : PointerEventBroker {

  /**
   * Callbacks for [PointerOverEvent]s
   */
  private val pointerOverCallbacks: MutableList<(PointerOverEvent) -> EventConsumption> = mutableListOf()

  override fun onOver(handler: (PointerOverEvent) -> EventConsumption) {
    pointerOverCallbacks.add(handler)
  }

  /**
   * Notify this broker about [pointerEvent]
   */
  fun notifyOver(pointerEvent: PointerOverEvent): EventConsumption {
    return pointerOverCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(pointerEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Callbacks for [PointerEnterEvent]s
   */
  private val pointerEnterCallbacks: MutableList<(PointerEnterEvent) -> EventConsumption> = mutableListOf()

  override fun onEnter(handler: (PointerEnterEvent) -> EventConsumption) {
    pointerEnterCallbacks.add(handler)
  }

  /**
   * Notify this broker about [pointerEvent]
   */
  fun notifyEnter(pointerEvent: PointerEnterEvent): EventConsumption {
    return pointerEnterCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(pointerEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Callbacks for [PointerDownEvent]s
   */
  private val pointerDownCallbacks: MutableList<(PointerDownEvent) -> EventConsumption> = mutableListOf()

  override fun onDown(handler: (PointerDownEvent) -> EventConsumption) {
    pointerDownCallbacks.add(handler)
  }

  /**
   * Notify this broker about [pointerEvent]
   */
  fun notifyDown(pointerEvent: PointerDownEvent): EventConsumption {
    return pointerDownCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(pointerEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Callbacks for [PointerMoveEvent]s
   */
  private val pointerMoveCallbacks: MutableList<(PointerMoveEvent) -> EventConsumption> = mutableListOf()

  override fun onMove(handler: (PointerMoveEvent) -> EventConsumption) {
    pointerMoveCallbacks.add(handler)
  }

  /**
   * Notify this broker about [pointerEvent]
   */
  fun notifyMove(pointerEvent: PointerMoveEvent): EventConsumption {
    return pointerMoveCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(pointerEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Callbacks for [PointerUpEvent]s
   */
  private val pointerUpCallbacks: MutableList<(PointerUpEvent) -> EventConsumption> = mutableListOf()

  override fun onUp(handler: (PointerUpEvent) -> EventConsumption) {
    pointerUpCallbacks.add(handler)
  }

  /**
   * Notify this broker about [pointerEvent]
   */
  fun notifyUp(pointerEvent: PointerUpEvent): EventConsumption {
    return pointerUpCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(pointerEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Callbacks for [PointerCancelEvent]s
   */
  private val pointerCancelCallbacks: MutableList<(PointerCancelEvent) -> EventConsumption> = mutableListOf()

  override fun onCancel(handler: (PointerCancelEvent) -> EventConsumption) {
    pointerCancelCallbacks.add(handler)
  }

  /**
   * Notify this broker about [pointerEvent]
   */
  fun notifyCancel(pointerEvent: PointerCancelEvent): EventConsumption {
    return pointerCancelCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(pointerEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Callbacks for [PointerOutEvent]s
   */
  private val pointerOutCallbacks: MutableList<(PointerOutEvent) -> EventConsumption> = mutableListOf()

  override fun onOut(handler: (PointerOutEvent) -> EventConsumption) {
    pointerOutCallbacks.add(handler)
  }

  /**
   * Notify this broker about [pointerEvent]
   */
  fun notifyOut(pointerEvent: PointerOutEvent): EventConsumption {
    return pointerOutCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(pointerEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Callbacks for [PointerLeaveEvent]s
   */
  private val pointerLeaveCallbacks: MutableList<(PointerLeaveEvent) -> EventConsumption> = mutableListOf()

  override fun onLeave(handler: (PointerLeaveEvent) -> EventConsumption) {
    pointerLeaveCallbacks.add(handler)
  }

  /**
   * Notify this broker about [pointerEvent]
   */
  fun notifyLeave(pointerEvent: PointerLeaveEvent): EventConsumption {
    return pointerLeaveCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(pointerEvent)
    } ?: EventConsumption.Ignored
  }

}
