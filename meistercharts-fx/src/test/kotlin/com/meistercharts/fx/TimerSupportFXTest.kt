package com.meistercharts.fx

import assertk.*
import assertk.assertions.*
import it.neckar.open.time.VirtualNowProvider
import it.neckar.open.javafx.FxUtils
import it.neckar.open.javafx.JavaFxTimer
import it.neckar.open.javafx.JavaFxTimerDebug
import it.neckar.open.javafx.test.JavaFxTest
import it.neckar.open.test.utils.VirtualTime
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

@JavaFxTest
class TimerSupportFXTest {

  @VirtualTime(5000.0)
  @Test
  fun testIt(nowProvider: VirtualNowProvider) {
    val beforeCount = JavaFxTimerDebug.findRegisteredAnimationTimers().size
    assertThat(beforeCount).isEqualTo(0)
    val timerSupport = TimerSupportFX()

    assertThat(JavaFxTimerDebug.findRegisteredAnimationTimers().size).isEqualTo(beforeCount)

    val run = AtomicBoolean(false)

    timerSupport.delay(1.0.milliseconds) {
      assertThat(run.get()).isFalse()
      run.set(true)
    }

    JavaFxTimer.waitForPaintPulse()
    assertThat(JavaFxTimerDebug.findRegisteredAnimationTimers().size).isEqualTo(beforeCount + 1)

    nowProvider.add(10.0)

    JavaFxTimer.waitForPaintPulse()
    FxUtils.waitFor {
      run.get()
    }

    assertThat(JavaFxTimerDebug.findRegisteredAnimationTimers().size).isEqualTo(beforeCount)
  }
}
