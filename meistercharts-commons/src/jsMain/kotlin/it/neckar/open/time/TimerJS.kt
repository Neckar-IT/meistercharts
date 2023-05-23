package it.neckar.open.time

import it.neckar.open.dispose.Disposable
import kotlinx.browser.window
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Sets a timer which executes a function once the timer expires.
 */
actual fun delay(delay: Duration, callback: () -> Unit): Disposable {
  return TimerId(window.setTimeout(callback, delay.toInt(DurationUnit.MILLISECONDS)))
}

/**
 * Repeats the given lambda every [delay] on the main thread
 */
actual fun repeat(delay: Duration, callback: () -> Unit): Disposable {
  return TimerId(window.setInterval(callback, delay.toInt(DurationUnit.MILLISECONDS)))
}

/**
 * Represents a timer id which may be canceled
 */
data class TimerId(val id: Int) : Disposable {
  override fun dispose() {
    window.clearTimeout(id)
  }
}
