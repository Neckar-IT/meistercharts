package com.meistercharts.events

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Describes whether an event has been consumed.
 * This enum can be used as return type for methods that possible consume events
 */
enum class EventConsumption {
  /**
   * The event has not been consumed but ignored by the handler/listener/...
   */
  Ignored,

  /**
   * The event has been consumed
   */
  Consumed;

  /**
   * Returns true if the event has been consumed
   */
  val consumed: Boolean
    get() = this == Consumed

  companion object {
    /**
     * Returns [Consumed] if the given boolean is true, [Ignored] otherwise
     */
    fun consumeIf(consumed: Boolean): EventConsumption {
      return if (consumed) Consumed else Ignored
    }

    inline fun consumeIf(consumed: () -> Boolean): EventConsumption {
      contract {
        callsInPlace(consumed, InvocationKind.EXACTLY_ONCE)
      }
      return consumeIf(consumed())
    }
  }
}
