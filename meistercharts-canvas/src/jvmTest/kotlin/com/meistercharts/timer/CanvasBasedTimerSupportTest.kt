/**
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
package com.meistercharts.timer

import com.meistercharts.canvas.timer.CanvasBasedTimerImplementation
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.test.utils.VirtualTime
import it.neckar.open.time.VirtualNowProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class CanvasBasedTimerSupportTest {

  private val timerSupport = CanvasBasedTimerImplementation()

  @VirtualTime
  @Test
  fun `delay function calls the callback after the delay`(nowProvider: VirtualNowProvider) {
    var wasCalled = false

    timerSupport.delay(1.seconds) {
      wasCalled = true
    }

    assertThat(wasCalled).isFalse()

    nowProvider.advanceBy(999.0)
    timerSupport.update(nowProvider.nowMillis())

    assertThat(wasCalled).isFalse()

    nowProvider.advanceBy(2.0)
    timerSupport.update(nowProvider.nowMillis())

    assertTrue(wasCalled, "The callback should have been called after the delay")
  }

  @VirtualTime
  @Test
  fun `repeat function calls the callback multiple times`(nowProvider: VirtualNowProvider) {
    var callCount = 0

    timerSupport.repeat(1.seconds) {
      callCount++
    }

    nowProvider.advanceBy(0.0)
    timerSupport.update(nowProvider.nowMillis())

    assertThat(callCount).isEqualTo(0)

    nowProvider.advanceBy(1001.0)
    timerSupport.update(nowProvider.nowMillis())


    assertThat(callCount).isEqualTo(1)

    nowProvider.advanceBy(1001.0)
    timerSupport.update(nowProvider.nowMillis())

    assertThat(callCount).isEqualTo(2)
  }

  @VirtualTime
  @Test
  fun `delay function calls all callbacks after their respective delays`(nowProvider: VirtualNowProvider) {
    val callbacksCalled = mutableListOf<Boolean>()

    //Register callbacks
    5.fastFor { i ->
      callbacksCalled.add(false)

      timerSupport.delay((i + 1).seconds) {
        callbacksCalled[i] = true
      }
    }

    // Check that callback has not been called immediately
    assertThat(callbacksCalled).containsOnly(false)

    nowProvider.advanceBy(500.0) //500.0
    assertThat(nowProvider.advancedTime()).isEqualTo(500.0)

    // Check that callback has not been called immediately
    assertThat(callbacksCalled).containsOnly(false)

    // Advance time and check callbacks one by one
    5.fastFor { i ->
      assertThat(callbacksCalled[i]).isFalse()
      timerSupport.update(nowProvider.nowMillis())
      assertThat(callbacksCalled[i]).isFalse()

      // Advance time past when the next callback is due
      nowProvider.advanceBy(1000.0)
      timerSupport.update(nowProvider.nowMillis())

      assertThat(callbacksCalled[i]).isTrue()
    }
  }

}


