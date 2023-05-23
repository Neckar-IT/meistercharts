package it.neckar.open.time

import it.neckar.open.dispose.Disposable
import kotlinx.coroutines.*
import kotlin.time.Duration

/**
 * Sets a timer which executes a function once the timer expires.
 * @return an id which identifies the timer. That id may be used to cancel the timer (see [cancel].
 */
actual fun delay(delay: Duration, callback: () -> Unit): Disposable {
  return jvmTimerSupport.delay(delay, callback)
}

/**
 * Repeats the given lambda every [delay]
 */
actual fun repeat(delay: Duration, callback: () -> Unit): Disposable {
  return jvmTimerSupport.repeat(delay, callback)
}

/**
 * Supports the time related methods for JVM
 */
interface JvmTimerSupport {
  fun delay(delay: Duration, callback: () -> Unit): Disposable
  fun repeat(delay: Duration, callback: () -> Unit): Disposable
}

var jvmTimerSupport: JvmTimerSupport = object : JvmTimerSupport {
  override fun delay(delay: Duration, callback: () -> Unit): Disposable {
    throw UnsupportedOperationException("please set the jvmTimerSupport for the current platform by calling QuickChartPlatform.init()")
  }

  override fun repeat(delay: Duration, callback: () -> Unit): Disposable {
    throw UnsupportedOperationException("please set the jvmTimerSupport for the current platform by calling QuickChartPlatform.init()")
  }
}
