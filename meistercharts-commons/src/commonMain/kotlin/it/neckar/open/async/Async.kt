package it.neckar.open.async

import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.time.delay
import kotlin.time.Duration

/**
 * Type-Alias for lambdas that can be executed
 */
typealias Runnable = () -> Unit


/**
 * This class can be used to for async method calls
 *
 */
class Async : Disposable {
  private val scheduledRunnables = HashMap<Any, Runnable>()

  /**
   * Executes the given runnable - but only the last one for a key after each given window.
   * This method can be called multiple times but is only executed once per [window].
   *
   * The *last* scheduled runnable is executed within each time window.
   */
  fun throttleLast(window: Duration, key: Any, runnable: Runnable) {
    if (scheduledRunnables.put(key, runnable) != null) {
      //There is another job scheduled, so we do not have to reschedule it
      return
    }

    //There has no other event been scheduled
    runDelayed(window) {
      getAndRemove(key)?.invoke()
    }.also {
      disposeSupport.onDispose(it)
    }
  }

  /**
   * Removes all runnables scheduled for execution with the given [key]
   */
  fun remove(key: Any) {
    scheduledRunnables.remove(key)
  }

  /**
   * Returns the runnable for the given key
   */
  private fun getAndRemove(key: Any): Runnable? {
    return scheduledRunnables.remove(key)
  }

  /**
   * Run in target thread
   */
  private fun runDelayed(delay: Duration, runnable: Runnable): Disposable {
    return delay(delay) {
      runnable()
    }
  }

  private val disposeSupport = DisposeSupport()

  override fun dispose() {
    scheduledRunnables.clear()
    disposeSupport.dispose()
  }
}
