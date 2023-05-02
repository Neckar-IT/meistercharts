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
package com.meistercharts.fx

import it.neckar.open.async.Async
import it.neckar.open.time.nowMillis
import it.neckar.open.javafx.JavaFxTimer
import it.neckar.open.test.utils.DisableIfHeadless
import javafx.application.Platform
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import kotlin.time.Duration.Companion.milliseconds

/**
 */
@DisableIfHeadless
class AsyncDemo : ApplicationTest() {
  @Test
  fun testIt() {
    println("Start: ${nowMillis()}")

    val async = Async()
    async.throttleLast(1000.0.milliseconds, "key") {
      println("Delayed 1: ${nowMillis()}")
    }
    async.throttleLast(1000.0.milliseconds, "key") {
      println("Delayed 2: ${nowMillis()}")
    }
    async.throttleLast(1000.0.milliseconds, "key") {
      println("Delayed 3: ${nowMillis()}")
    }


    Platform.runLater {
      println("in fx thread")
    }

    println("waiting <${nowMillis()}>")
    Thread.sleep(2000)
    println("done <${nowMillis()}>")

    async.dispose()
  }

  @Test
  fun testDelay() {
    Platform.runLater {
      MeisterChartsPlatform.init()

      var lastCall = 0L

      val async = Async()

      async.throttleLast(100.milliseconds, "asdf") {
        val now = System.currentTimeMillis()
        val delta = lastCall - now
        println("In Delay @ $now - after $delta")

        lastCall = now
      }
    }

    Thread.sleep(1000000L)
  }

  @Test
  fun testRepeat() {
    Platform.runLater {
      MeisterChartsPlatform.init()

      var lastCall = 0L

      JavaFxTimer.repeat(17.milliseconds) {
        val now = System.currentTimeMillis()
        val delta = lastCall - now
        println("In Delay @ $now - after $delta")

        lastCall = now
      }
    }

    Thread.sleep(1000000L)
  }
}
