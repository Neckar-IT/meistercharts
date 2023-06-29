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
package com.meistercharts.charts.timeline

import com.meistercharts.history.HistoryStorage
import it.neckar.open.kotlin.lang.ifNaN
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.unit.si.ms

/**
 * Provides the youngest time from a history storage
 */
class YoungestTimeProvider(
  /**
   * The history storage that is used to extract the time from
   */
  val historyStorage: HistoryStorage,
  /**
   * The fallback is used, if the history storage does not provide a youngest time
   */
  val fallback: @ms DoubleProvider = DoubleProvider.nowMillis,
) : @ms DoubleProvider {
  override fun invoke(): Double {
    return historyStorage.getEnd().ifNaN { fallback() }
  }
}

