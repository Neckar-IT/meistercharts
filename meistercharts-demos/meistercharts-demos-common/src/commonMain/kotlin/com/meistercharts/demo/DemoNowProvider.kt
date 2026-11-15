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
package com.meistercharts.demo

import it.neckar.open.formatting.formatUtc
import it.neckar.open.kotlin.lang.requireFinite
import it.neckar.open.time.ClockNowProvider
import it.neckar.open.time.NowProvider
import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.si.ms

/**
 * Provides a fixed point in time for now.
 */
@Deprecated("Does not make any sense... Do not use anymore")
class DemoNowProvider : NowProvider {
  /**
   * When the demo has been started
   */
  val started: @ms Double

  /**
   * The difference between started and the reference time
   */
  val offsetBetweenStartedAndReferenceTime: @ms Double

  init {
    val now = ClockNowProvider.nowMillis().requireFinite()
    started = now
    offsetBetweenStartedAndReferenceTime = (started - referenceTime).requireFinite()
  }

  override fun nowMillis(): @ms @IsFinite Double {
    return ClockNowProvider.nowMillis() - offsetBetweenStartedAndReferenceTime
  }

  companion object {
    val referenceTime: @ms Double = (1686042664231.0).also {
      require(it.formatUtc().startsWith("2023-06-06T09:11:04.231")) { //JS has as "Z" at the end
        "Was <${it.formatUtc()}>"
      }
    }
  }
}
