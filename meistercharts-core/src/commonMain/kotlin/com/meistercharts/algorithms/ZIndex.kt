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
package com.meistercharts.algorithms

import it.neckar.open.unit.number.PositiveOrZero
import kotlin.jvm.JvmInline

/**
 * Represents the Z-Index.
 *
 * * High values: Placed at the top
 * * Low values: Placed at the bottom
 */
@com.meistercharts.annotations.ZIndex
@JvmInline
value class ZIndex(val value: @PositiveOrZero Double) : Comparable<ZIndex> {
  override fun compareTo(other: ZIndex): Int {
    return this.value.compareTo(other.value)
  }

  companion object {
    /**
     * Represents the "lowest" z-index that is assigned by default
     */
    val auto: ZIndex = ZIndex(0.0)
  }
}
