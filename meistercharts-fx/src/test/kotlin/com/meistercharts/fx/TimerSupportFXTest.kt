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
package com.meistercharts.fx

import it.neckar.open.javafx.test.JavaFxTest

@JavaFxTest
class TimerSupportFXTest {

  //JavaFxTimerDebug requires internal classes and has been moved to attic
  //@VirtualTime(5000.0)
  //@Test
  //@Timeout(value = 10, unit = TimeUnit.SECONDS)
  //fun testIt(nowProvider: VirtualNowProvider) {
  //  val beforeCount = JavaFxTimerDebug.findRegisteredAnimationTimers().size
  //  assertThat(beforeCount).isEqualTo(0)
  //  val timerSupport = TimerImplementationFX()
  //
  //  assertThat(JavaFxTimerDebug.findRegisteredAnimationTimers().size).isEqualTo(beforeCount)
  //
  //  val run = AtomicBoolean(false)
  //
  //  timerSupport.delay(1.0.milliseconds) {
  //    assertThat(run.get()).isFalse()
  //    run.set(true)
  //  }
  //
  //  JavaFxTimer.waitForPaintPulse()
  //  assertThat(JavaFxTimerDebug.findRegisteredAnimationTimers().size).isEqualTo(beforeCount + 1)
  //
  //  nowProvider.advanceBy(10.0)
  //
  //  JavaFxTimer.waitForPaintPulse()
  //  FxUtils.waitFor {
  //    run.get()
  //  }
  //
  //  assertThat(JavaFxTimerDebug.findRegisteredAnimationTimers().size).isEqualTo(beforeCount)
  //}
}
