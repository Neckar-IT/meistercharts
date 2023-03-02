package com.meistercharts.fx

import it.neckar.open.javafx.JavaFxTimer
import it.neckar.open.dispose.Disposable
import it.neckar.open.time.JvmTimerSupport
import kotlin.time.Duration

/**
 * Java FX implementation for timer related stuff
 */
class TimerSupportFX : JvmTimerSupport {

  override fun delay(delay: Duration, callback: () -> Unit): Disposable {
    return JavaFxTimer.delay(delay, callback)
  }

  override fun repeat(delay: Duration, callback: () -> Unit): Disposable {
    return JavaFxTimer.repeat(delay, callback)
  }
}
