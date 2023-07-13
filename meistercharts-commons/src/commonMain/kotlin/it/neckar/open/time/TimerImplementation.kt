package it.neckar.open.time

import it.neckar.open.dispose.Disposable
import kotlin.time.Duration

/**
 * Callback that can be set to support [repeat] and [delay] methods.
 */
interface TimerImplementation {
  /**
   * Calls the callback after the provided [delay].]
   */
  fun delay(delay: Duration, callback: () -> Unit): Disposable

  fun repeat(delay: Duration, callback: () -> Unit): Disposable
}
