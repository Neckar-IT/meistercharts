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
import it.neckar.open.i18n.DefaultI18nConfiguration
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.other.Inclusive

/**
 * Represents a tile index
 */
data class TileIndex(
  val mainX: MainIndex,
  val subX: SubIndex,
  val mainY: MainIndex,
  val subY: SubIndex,
) {
  /**
   * Returns a string representation of the tile index, formatted as "mainX.subX/mainY.subY".
   *
   * @return A string representation of the tile index.
   */
  override fun toString(): String {
    return format(intFormat, I18nConfiguration.US)
  }

  /**
   * Returns a new TileIndex that is one unit above this TileIndex.
   */
  inline fun above(): TileIndex {
    return this.previousY()
  }

  /**
   * Returns a new TileIndex that is one unit below this TileIndex.
   */
  inline fun below(): TileIndex {
    return this.nextY()
  }

  /**
   * Returns a new TileIndex that is one unit to the left of this TileIndex.
   */
  inline fun leftOf(): TileIndex {
    return this.previousX()
  }

  /**
   * Returns a new TileIndex that is one unit to the right of this TileIndex.
   */
  inline fun rightOf(): TileIndex {
    return this.nextX()
  }

  /**
   * Checks if this TileIndex is to the left of the other TileIndex.
   * @return True if this TileIndex is to the left, false otherwise.
   */
  fun isLeftOf(other: TileIndex): Boolean {
    if (this.mainX < other.mainX) {
      return true
    } else if (this.mainX == other.mainX) {
      return this.subX < other.subX
    }
    return false
  }

  /**
   * Checks if this TileIndex is above the other TileIndex.
   * @return True if this TileIndex is above, false otherwise.
   */
  fun isAbove(other: TileIndex): Boolean {
    if (this.mainY < other.mainY) {
      return true
    } else if (this.mainY == other.mainY) {
      return this.subY < other.subY
    }
    return false
  }

  /**
   * Checks if this TileIndex is to the right of the given TileIndex.
   * @return True if this TileIndex is to the right, false otherwise.
   */
  fun isRightOf(other: TileIndex): Boolean {
    return when {
      this.mainX > other.mainX -> true
      this.mainX < other.mainX -> false
      else -> this.subX > other.subX
    }
  }

  /**
   * Checks if this TileIndex is below the given TileIndex.
   * @return True if this TileIndex is below, false otherwise.
   */
  fun isBelow(other: TileIndex): Boolean {
    return when {
      this.mainY > other.mainY -> true
      this.mainY < other.mainY -> false
      else -> this.subY > other.subY
    }
  }

  /**
   * Checks if this TileIndex is to the left of or equal to the other TileIndex.
   */
  fun isLeftOfOrEqual(other: TileIndex): Boolean {
    return this.isLeftOf(other) || this == other
  }

  /**
   * Checks if this TileIndex is above or equal to the other TileIndex.
   */
  fun isAboveOrEqual(other: TileIndex): Boolean {
    return this.isAbove(other) || this == other
  }

  /**
   * Checks if this TileIndex is to the right of or equal to the other TileIndex.
   */
  fun isRightOfOrEqual(other: TileIndex): Boolean {
    return this.isRightOf(other) || this == other
  }

  /**
   * Checks if this TileIndex is below or equal to the other TileIndex.
   */
  fun isBelowOrEqual(other: TileIndex): Boolean {
    return this.isBelow(other) || this == other
  }

  /**
   * Returns a string representation of the tile index, formatted according to the provided [NumberFormat].
   *
   * @param format The [NumberFormat] to use when formatting the string. Defaults to [intFormat].
   * @param i18nConfiguration The [I18nConfiguration] to use when formatting the string. Defaults to [DefaultI18nConfiguration].
   * @return A string representation of the tile index, formatted according to the provided [NumberFormat].
   */
  fun format(format: NumberFormat = intFormat, i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration): String {
    return "${mainX.format(format, i18nConfiguration)}:${subX.format(format, i18nConfiguration)}/${mainY.format(format, i18nConfiguration)}:${subY.format(format, i18nConfiguration)}"
  }

  fun nextX(): TileIndex {
    return if (subX.atMax()) {
      TileIndex(mainX.increment(), SubIndex(0), mainY, subY)
    } else {
      TileIndex(mainX, subX.increment(), mainY, subY)
    }
  }

  fun previousX(): TileIndex {
    return if (subX.atMin()) {
      TileIndex(mainX.decrement(), SubIndex(SubIndexFactor - 1), mainY, subY)
    } else {
      TileIndex(mainX, subX.decrement(), mainY, subY)
    }
  }

  // Move to the next Y index
  fun nextY(): TileIndex {
    return if (subY.atMax()) {
      TileIndex(mainX, subX, mainY.increment(), SubIndex(0))
    } else {
      TileIndex(mainX, subX, mainY, subY.increment())
    }
  }

  // Move to the previous Y index
  fun previousY(): TileIndex {
    return if (subY.atMin()) {
      TileIndex(mainX, subX, mainY.decrement(), SubIndex(SubIndexFactor - 1))
    } else {
      TileIndex(mainX, subX, mainY, subY.decrement())
    }
  }

  /**
   * Checks if this TileIndex is within the rectangle defined by two other TileIndices.
   * @return True if this TileIndex is within the rectangle, false otherwise.
   */
  fun isWithin(topLeft: TileIndex, bottomRight: TileIndex): Boolean {
    require(bottomRight.isLeftOf(topLeft).not()) {
      "bottom right <$bottomRight> must not be left of top left <$topLeft>"
    }
    require(bottomRight.isAbove(topLeft).not()) {
      "bottom right <$bottomRight> must not be above top left <$topLeft>"
    }

    if (this.isLeftOf(topLeft)) {
      return false
    }
    if (this.isAbove(topLeft)) {
      return false
    }

    if (this.isRightOf(bottomRight)) {
      return false
    }
    if (this.isBelow(bottomRight)) {
      return false
    }

    return true
  }

  fun xAsDouble(): Double {
    return mainX.value * SubIndexFactor.toDouble() + subX.value
  }

  fun yAsDouble(): Double {
    return mainY.value * SubIndexFactor.toDouble() + subY.value
  }

  /**
   * Returns the x value as int.
   *
   * ATTENTION: This does *not* work for very large values of the index.
   * It does only work for small values.
   */
  fun xAsInt(): Int {
    require(mainX.value < Int.MAX_VALUE / SubIndexFactor) {
      "mainX value ${mainX.value} is too large to be converted to int"
    }

    if (mainX.value > Int.MAX_VALUE / SubIndexFactor) {
      throw IllegalStateException("mainX value ${mainX.value} is too large to be converted to int")
    }

    return mainX.value * SubIndexFactor + subX.value
  }

  fun yAsInt(): Int {
    if (mainY.value > Int.MAX_VALUE / SubIndexFactor) {
      throw IllegalStateException("mainY value ${mainY.value} is too large to be converted to int")
    }

    return mainY.value * SubIndexFactor + subY.value
  }

  companion object {
    const val SubIndexFactor: Int = 1_000

    val Origin: TileIndex = TileIndex(MainIndex(0), SubIndex(0), MainIndex(0), SubIndex(0))
    val Max: TileIndex = TileIndex(MainIndex.Max, SubIndex.Max, MainIndex.Max, SubIndex.Max)
    val Min: TileIndex = TileIndex(MainIndex.Min, SubIndex.Min, MainIndex.Min, SubIndex.Min)

    operator fun invoke(
      mainX: Int,
      subX: Int,
      mainY: Int,
      subY: Int,
    ): TileIndex {
      return TileIndex(MainIndex(mainX), SubIndex(subX), MainIndex(mainY), SubIndex(subY))
    }

    /**
     * Creates a new TileIndex from the given sub indices.
     */
    fun of(x: @MayBeNegative Int, y: @MayBeNegative Int): TileIndex {
      val mainX = MainIndex.calculateMainTileIndexPart(x.toDouble())
      val mainY = MainIndex.calculateMainTileIndexPart(y.toDouble())

      val subX = SubIndex.calculateSubTileIndexPart(x.toDouble())
      val subY = SubIndex.calculateSubTileIndexPart(y.toDouble())

      return invoke(mainX, subX, mainY, subY)
    }

    fun of(mainX: Int, subX: Int, mainY: Int, subY: Int): TileIndex {
      return TileIndex(MainIndex(mainX), SubIndex(subX), MainIndex(mainY), SubIndex(subY))
    }

    /**
     * Sorts the indices in x direction
     */
    val compareByX: Comparator<in TileIndex> = Comparator { o1, o2 ->
      val mainCompare = o1.mainX.compareTo(o2.mainX)
      if (mainCompare != 0) {
        mainCompare
      } else {
        o1.subX.compareTo(o2.subX)
      }
    }

    /**
     * Sorts indices in y direction
     */
    val compareByY: Comparator<TileIndex> = Comparator { o1, o2 ->
      val mainCompare = o1.mainY.compareTo(o2.mainY)
      if (mainCompare != 0) {
        mainCompare
      } else {
        o1.subY.compareTo(o2.subY)
      }
    }

    /**
     * Sorts indices first in y direction, then in x direction
     */
    val compareByRow: Comparator<TileIndex> = Comparator { o1, o2 ->
      val yCompare = compareByY.compare(o1, o2)
      if (yCompare != 0) {
        yCompare
      } else {
        compareByX.compare(o1, o2)
      }
    }

    /**
     * Iterates over the tile indices
     */
    fun iterateOverTileIndices(
      start: @Inclusive TileIndex,
      end: @Inclusive TileIndex,
      callback: (mainX: MainIndex, subX: SubIndex, mainY: MainIndex, subY: SubIndex) -> Unit,
    ) {
      for (mainX in start.mainX.value..end.mainX.value) {
        val startSubX = if (mainX == start.mainX.value) start.subX.value else 0
        val endSubX = if (mainX == end.mainX.value) end.subX.value else TileIndex.SubIndexFactor - 1

        for (subX in startSubX..endSubX) {
          for (mainY in start.mainY.value..end.mainY.value) {
            val startSubY = if (mainY == start.mainY.value) start.subY.value else 0
            val endSubY = if (mainY == end.mainY.value) end.subY.value else TileIndex.SubIndexFactor - 1

            for (subY in startSubY..endSubY) {
              callback(MainIndex(mainX), SubIndex(subX), MainIndex(mainY), SubIndex(subY))
            }
          }
        }
      }
    }
  }
}
