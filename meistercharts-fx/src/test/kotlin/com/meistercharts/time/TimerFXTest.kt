/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("RETURN_VALUE_NOT_USED") // Test scope: return values intentionally discarded

package com.meistercharts.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.javafx.FxUtils
import it.neckar.open.javafx.JavaFxTimer
import it.neckar.open.javafx.test.JavaFxTest
import it.neckar.open.javafx.test.assertFxThread
import it.neckar.open.javafx.test.assertNotFxThread
import it.neckar.open.test.utils.isFalse
import it.neckar.open.test.utils.isTrue
import it.neckar.open.time.delay
import com.meistercharts.fx.TimerImplementationFX
import it.neckar.open.time.timerImplementation
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

@Disabled
@JavaFxTest
class TimerFXTest {
  @BeforeEach
  fun setUp() {
    timerImplementation = TimerImplementationFX()
  }

  @Test
  internal fun testStart2() {
    var result = 0
    delay(10.milliseconds) { result = 5 }

    FxUtils.waitFor { result == 5 }
  }

  @Test
  internal fun testCancel2() {
    var result = 0
    val disposable = delay(5.milliseconds) {
      result = 5
    }

    FxUtils.waitFor {
      result == 5
    }

    disposable.dispose()
    val lastResult = result

    //Wait for a given time to ensure the result is no longer increased
    Thread.sleep(100)
    assertThat(result).isEqualTo(lastResult)
  }

  @Test
  internal fun testIt() {
    assertNotFxThread()

    val condition = AtomicBoolean()

    delay(100.milliseconds) {
      assertThat(condition).isFalse()
      condition.set(true)
      assertThat(condition).isTrue()

      assertFxThread()
    }

    FxUtils.waitFor {
      condition.get()
    }
    assertThat(condition).isTrue()
  }

  @Test
  internal fun testRepeat() {
    val counter = AtomicInteger()

    val disposable = it.neckar.open.time.repeat(16.milliseconds) {
      assertFxThread()
      counter.incrementAndGet()
    }

    try {
      FxUtils.waitFor {
        counter.get() > 4
      }

    } finally {
      disposable.dispose()
    }

    JavaFxTimer.waitForPaintPulse() //ensure repeat has been finished

    counter.set(0)
    JavaFxTimer.waitForPaintPulse() //ensure repeat has been finished

    assertThat(counter.get()).isEqualTo(0)
  }

  @Test
  internal fun testCancel() {
    assertNotFxThread()

    val condition = AtomicBoolean()

    delay(1.milliseconds) {
      fail("must not be called")
    }.dispose() //cancel immediately

    JavaFxTimer.waitForPaintPulse() //ensure repeat has been finished
    assertThat(condition).isFalse()
  }
}
