package it.neckar.open.time

import it.neckar.open.dispose.Disposable
import kotlinx.coroutines.*
import kotlin.time.Duration

/**
 * Timer support that uses Coroutines
 */
class JVMTimerCoroutineImplementation(
  /**
   * The scope that is used to delay the callbacks
   */
  val delayScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
  /**
   * The scope that is used to repeat the callbacks
   */
  val repeatScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : TimerImplementation {

  override fun delay(delay: Duration, callback: () -> Unit): Disposable {
    val job = delayScope.launch {
      delay(delay)
      callback()
    }
    return Disposable { job.cancel("Disposed of delayScope") }
  }

  override fun repeat(delay: Duration, callback: () -> Unit): Disposable {
    require(delay.inWholeMilliseconds >= 1) { "delay must be at least 1 millisecond but was $delay" }

    val job = repeatScope.launch {
      while (isActive) {
        callback()
        delay(delay)
      }
    }
    return Disposable { job.cancel("Disposed of delayScope") }
  }
}
