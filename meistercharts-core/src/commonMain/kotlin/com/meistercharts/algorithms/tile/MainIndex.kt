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
package com.meistercharts.algorithms.tile

import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.intFormat
import it.neckar.open.i18n.DefaultI18nConfiguration
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.toIntFloor
import kotlin.jvm.JvmInline

/**
 * Represents an index for a tile in a 2D space, providing additional precision through the use of sub-indices.
 */
@JvmInline
value class MainIndex(val value: Int) : Comparable<MainIndex> {
  fun format(format: NumberFormat = intFormat, i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration): String {
    return format.format(value.toDouble(), i18nConfiguration)
  }

  fun increment(): MainIndex {
    return MainIndex(value + 1)
  }

  fun decrement(): MainIndex {
    return MainIndex(value - 1)
  }

  // Checks if the MainIndex is at its minimum value.
  fun atMin(): Boolean {
    return value == Int.MIN_VALUE
  }

  // Checks if the MainIndex is at its maximum value.
  fun atMax(): Boolean {
    return value == Int.MAX_VALUE
  }

  override fun compareTo(other: MainIndex): Int {
    return this.value.compareTo(other.value)
  }

  operator fun times(factor: Double): Double {
    return factor * value
  }

  override fun toString(): String {
    return value.toString()
  }

  companion object {
    val Zero: MainIndex = MainIndex(0)
    val Max: MainIndex = MainIndex(Int.MAX_VALUE)
    val Min: MainIndex = MainIndex(Int.MIN_VALUE)

    /**
     * Calculate the sub index from an exact index
     */
    fun calculateMainTileIndexPart(indexAsDouble: Double): Int {
      return (indexAsDouble / TileIndex.SubIndexFactor).toIntFloor()
    }
  }
}
