package it.neckar.open.time

import it.neckar.open.dispose.Disposable
import kotlin.time.Duration

/**
 * Executes the given callback with a delay on the main thread.
 *
 * Returns a [Disposable] which may be used to cancel the timer
 */
fun delay(delay: Duration, callback: () -> Unit): Disposable {
  return timerImplementation.delay(delay, callback)
}

/**
 * Repeats the given lambda every [delay] on the main thread
 */
fun repeat(delay: Duration, callback: () -> Unit): Disposable {
  return timerImplementation.repeat(delay, callback)
}


/**
 * Current timer support
 */
var timerImplementation: TimerImplementation = object : TimerImplementation {
  override fun delay(delay: Duration, callback: () -> Unit): Disposable {
    throw UnsupportedOperationException("please set the timerSupport for the current platform")
  }

  override fun repeat(delay: Duration, callback: () -> Unit): Disposable {
    throw UnsupportedOperationException("please set the timerSupport for the current platform")
  }
}
