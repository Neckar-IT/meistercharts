package com.meistercharts.events

/**
 * Listener for pointer events.
 *
 * Implementations *consume* events
 */
interface PointerEventHandler {
  /**
   * Is notified when a [PointerOverEvent] is fired
   */
  fun onOver(event: PointerOverEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerEnterEvent] is fired
   */
  fun onEnter(event: PointerEnterEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerDownEvent] is fired
   */
  fun onDown(event: PointerDownEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerMoveEvent] is fired
   */
  fun onMove(event: PointerMoveEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerUpEvent] is fired
   */
  fun onUp(event: PointerUpEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerCancelEvent] is fired
   */
  fun onCancel(event: PointerCancelEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerOutEvent] is fired
   */
  fun onOut(event: PointerOutEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerLeaveEvent] is fired
   */
  fun onLeave(event: PointerLeaveEvent): EventConsumption {
    return EventConsumption.Ignored
  }
}

/**
 * Register the given event handler at the broker
 */
fun PointerEventBroker.register(eventHandler: PointerEventHandler) {
  onOver(eventHandler::onOver)
  onEnter(eventHandler::onEnter)
  onDown(eventHandler::onDown)
  onMove(eventHandler::onMove)
  onUp(eventHandler::onUp)
  onCancel(eventHandler::onCancel)
  onOut(eventHandler::onOut)
  onLeave(eventHandler::onLeave)
}
