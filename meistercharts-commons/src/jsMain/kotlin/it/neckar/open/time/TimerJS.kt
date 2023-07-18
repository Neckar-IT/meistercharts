package it.neckar.open.time

import it.neckar.open.dispose.Disposable
import kotlinx.browser.window
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Represents a timer id which may be canceled
 */
data class TimerId(val id: Int) : Disposable {
  override fun dispose() {
    window.clearTimeout(id)
  }
}

/**
 * Uses the `window.setTimeout` and `window.setInterval` methods to implement the [TimerImplementation] interface.
 *
 * For meistercharts a own implementation exists that uses the canvas and render listeners.
 */
class JsWindowSetIntervalTimerImplementation : TimerImplementation {
  override fun delay(delay: Duration, callback: () -> Unit): Disposable {
    return TimerId(window.setTimeout(callback, delay.toInt(DurationUnit.MILLISECONDS)))
  }

  override fun repeat(delay: Duration, callback: () -> Unit): Disposable {
    return TimerId(window.setInterval(callback, delay.toInt(DurationUnit.MILLISECONDS)))
  }
}
