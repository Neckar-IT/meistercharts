package com.meistercharts.events

import it.neckar.open.unit.si.ms

/**
 * A platform-independent ui-related event that can be consumed
 */
abstract class UiEvent(
  /**
   * When the event has occurred
   */
  @ms
  val timestamp: Double
) {
}
