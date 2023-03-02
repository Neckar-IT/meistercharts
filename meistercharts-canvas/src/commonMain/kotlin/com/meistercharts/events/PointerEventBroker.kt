package com.meistercharts.events

import com.meistercharts.events.EventConsumption
import com.meistercharts.events.PointerCancelEvent
import com.meistercharts.events.PointerDownEvent
import com.meistercharts.events.PointerEnterEvent
import com.meistercharts.events.PointerLeaveEvent
import com.meistercharts.events.PointerMoveEvent
import com.meistercharts.events.PointerOutEvent
import com.meistercharts.events.PointerOverEvent
import com.meistercharts.events.PointerUpEvent

/**
 * Offers a way to register listeners for pointer events
 *
 * Instances *provide* events
 */
@Deprecated("Pointer events are not supported")
interface PointerEventBroker {
  /**
   * Registers a lambda that is notified when a [PointerOverEvent] is fired
   */
  fun onOver(handler: (PointerOverEvent) -> EventConsumption)

  /**
   * Registers a lambda that is notified when a [PointerEnterEvent] is fired
   */
  fun onEnter(handler: (PointerEnterEvent) -> EventConsumption)

  /**
   * Registers a lambda that is notified when a [PointerDownEvent] is fired
   */
  fun onDown(handler: (PointerDownEvent) -> EventConsumption)

  /**
   * Registers a lambda that is notified when a [PointerMoveEvent] is fired
   */
  fun onMove(handler: (PointerMoveEvent) -> EventConsumption)

  /**
   * Registers a lambda that is notified when a [PointerUpEvent] is fired
   */
  fun onUp(handler: (PointerUpEvent) -> EventConsumption)

  /**
   * Registers a lambda that is notified when a [PointerCancelEvent] is fired
   */
  fun onCancel(handler: (PointerCancelEvent) -> EventConsumption)

  /**
   * Registers a lambda that is notified when a [PointerOutEvent] is fired
   */
  fun onOut(handler: (PointerOutEvent) -> EventConsumption)

  /**
   * Registers a lambda that is notified when a [PointerLeaveEvent] is fired
   */
  fun onLeave(handler: (PointerLeaveEvent) -> EventConsumption)
}
