package it.neckar.open.time

import kotlin.js.Date

/**
 * Formats a double value (in milliseconds) as UTC string.
 * This method must only be used for debugging purposes.
 */
actual fun Double.formatUtcForDebug(): String {
  if (this.isNaN()) {
    return "NaN"
  }

  if (this.isInfinite()) {
    return "∞"
  }

  return Date(this).toISOString()
}
