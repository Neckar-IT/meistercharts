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
package com.meistercharts.history.generator

import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.impl.HistoryChunk
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms

/**
 * Provides history chunks
 * //TODO: continue!
 */
interface HistoryChunkProvider {

  val historyConfiguration: HistoryConfiguration

  /**
   * Creates the next [HistoryChunk]
   */
  fun next(until: @ms Double = nowMillis()): HistoryChunk?

  companion object {
  }
}
