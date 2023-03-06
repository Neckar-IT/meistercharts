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
