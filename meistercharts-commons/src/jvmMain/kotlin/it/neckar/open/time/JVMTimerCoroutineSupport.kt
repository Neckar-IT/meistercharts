package it.neckar.open.time

import it.neckar.open.dispose.Disposable
import kotlinx.coroutines.*
import kotlin.time.Duration

class JVMTimerCoroutineSupport(
  val delayScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
  val repeatScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : JvmTimerSupport {
  override fun delay(delay: Duration, callback: () -> Unit): Disposable {
    val job = delayScope.launch {
      delay(delay)
      callback()
    }
    return Disposable { job.cancel("Disposed of delayScope") }
  }

  override fun repeat(delay: Duration, callback: () -> Unit): Disposable {
    require(delay.inWholeMilliseconds >= 1) { "delay must be at least 1 millisecond but was $delay" }

    repeatScope.launch {
      while (isActive) {
        callback()
        delay(delay)
      }
    }
    return Disposable { repeatScope.cancel("Disposed of delayScope") }
  }
}
