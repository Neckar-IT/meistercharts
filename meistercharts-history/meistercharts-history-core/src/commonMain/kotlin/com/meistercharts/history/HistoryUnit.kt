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
package com.meistercharts.history

import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.mm
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a unit
 */
@Serializable
@JvmInline
value class HistoryUnit(val name: String?) {

  companion object {
    /**
     * Empty unit
     */
    val None: HistoryUnit = HistoryUnit(null)

    val pct: @pct HistoryUnit = HistoryUnit("percent")

    /**
     * millimeter
     */
    val mm: @mm HistoryUnit = HistoryUnit("mm")
    val ml: @mm HistoryUnit = HistoryUnit("ml")
  }
}
