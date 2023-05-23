package it.neckar.open.async

import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.OnDispose
import kotlin.time.Duration

/**
 * A class that supports the starting of timers which are automatically disposed
 * when [onDispose] has been disposed.
 */
class TimerSupport(private val onDispose: OnDispose) {

  /**
   * Used for [throttleLast]
   */
  private val async: Async = Async().also {
    onDispose.onDispose(it)
  }

  /**
   * Executes the given callback with a delay on the main thread.
   *
   * Returns a [Disposable] which may be used to cancel the timer
   * @return a disposable that can be used to cancel the timer.
   */
  fun delay(delay: Duration, callback: () -> Unit): Disposable {
    return it.neckar.open.time.delay(delay, callback).also {
      onDispose.onDispose(it)
    }
  }

  /**
   * Repeats the given lambda every [delay] on the main thread
   */
  fun repeat(delay: Duration, callback: () -> Unit): Disposable {
    return it.neckar.open.time.repeat(delay, callback).also {
      onDispose.onDispose(it)
    }
  }

  /**
   * Executes the given runnable - but only the last one for a key after each given window.
   * This method can be called multiple times but is only executed once per [window].
   *
   * The *last* scheduled runnable is executed within each time window.
   */
  fun throttleLast(window: Duration, key: Any, runnable: () -> Unit) {
    async.throttleLast(window, key, runnable)
  }
}
