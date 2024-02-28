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
package com.meistercharts.tile

import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.intFormat
import it.neckar.open.i18n.CurrentI18nConfiguration
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.toIntFloor
import it.neckar.open.unit.number.PositiveOrZero
import kotlin.jvm.JvmInline

/**
 * Represents a sub index
 */
@JvmInline
value class SubIndex(val value: @PositiveOrZero Int) : Comparable<SubIndex> {
  init {
    require(value >= 0) {
      "Must be positive but was $value"
    }
  }

  fun format(format: NumberFormat = intFormat, i18nConfiguration: I18nConfiguration = CurrentI18nConfiguration): String {
    return format.format(value.toDouble(), i18nConfiguration)
  }

  // Add one to the sub index, considering the sub index factor.
  fun increment(): SubIndex {
    return SubIndex((value + 1) % TileIndex.SubIndexFactor)
  }

  // Subtract one from the sub index, considering the sub index factor.
  fun decrement(): SubIndex {
    return SubIndex(if (value - 1 >= 0) value - 1 else TileIndex.SubIndexFactor - 1)
  }

  // Checks if the SubIndex is at its minimum value.
  fun atMin(): Boolean {
    return value == 0
  }

  // Checks if the SubIndex is at its maximum value.
  fun atMax(): Boolean {
    return value == TileIndex.SubIndexFactor - 1
  }

  operator fun times(factor: Double): Double {
    return factor * value
  }

  override fun compareTo(other: SubIndex): Int {
    return this.value.compareTo(other.value)
  }

  override fun toString(): String {
    return value.toString()
  }

  operator fun minus(subtrahend: Int): SubIndex {
    return SubIndex(value - subtrahend)
  }

  companion object {
    /**
     * Calculate the sub index from an exact index
     */
    fun calculateSubTileIndexPart(subIndexAsDouble: Double): Int {
      return (subIndexAsDouble.toIntFloor().rem(TileIndex.SubIndexFactor) + TileIndex.SubIndexFactor) % TileIndex.SubIndexFactor
    }

    val Zero: SubIndex = SubIndex(0)
    val Min: SubIndex = Zero
    val Max: SubIndex = SubIndex(TileIndex.SubIndexFactor - 1)
  }
}
