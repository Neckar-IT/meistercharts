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
package com.meistercharts.algorithms.layout

import com.meistercharts.annotations.Zoomed
import it.neckar.open.kotlin.lang.toIntFloor
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmInline

/**
 * A layout that contains locations/sizes of "boxes" (which might filled for example with bars).
 *
 * Attention: Each box has the same size
 */
class EquisizedBoxLayout(
  /**
   * The total amount of space that is available
   */
  val availableSpace: @px Double,
  /**
   * The number of blocks
   */
  val numberOfBoxes: Int,
  /**
   * Returns the size (width or height) for every box
   */
  val boxSize: @Zoomed Double,
  /**
   * The gap between two boxes (not at the outsides)
   */
  val gap: @px Double,
  /**
   * The direction in which the categories are laid out
   */
  val layoutDirection: LayoutDirection,
) {

  /**
   * The remaining space when the maximum size has been reached (without gaps)
   */
  val remainingSpace: @px Double
    get() {
      return availableSpace - usedSpace
    }

  /**
   * The total amount of used space for all boxes and gaps
   */
  val usedSpace: @px Double
    get() {
      return usedSpaceWithoutGaps + totalGaps
    }

  /**
   * The space required for all gaps
   */
  val totalGaps: @px Double
    get() {
      return (numberOfBoxes - 1) * gap
    }

  /**
   * The used space without any gaps
   */
  val usedSpaceWithoutGaps: @px Double
    get() {
      return numberOfBoxes * boxSize
    }

  /**
   * Calculates the center (x or y depending on the orientation) for the
   * box with the given index.
   *
   * ORIGIN is at the *left* or *top*
   */
  fun calculateCenter(index: BoxIndex): @Zoomed Double {
    @Zoomed val offset = calculateOffset()
    return offset + boxSize * (index.value + 0.5) + gap * index.value
  }

  /**
   * Calculates the offset depending on the layout direction
   */
  private fun calculateOffset(): @Zoomed Double {
    @Zoomed val offset = when (layoutDirection) {
      LayoutDirection.CenterHorizontal -> remainingSpace / 2.0
      LayoutDirection.CenterVertical -> remainingSpace / 2.0
      LayoutDirection.LeftToRight -> 0.0 // Default direction no offset
      LayoutDirection.RightToLeft -> remainingSpace //to screen negative
      LayoutDirection.TopToBottom -> 0.0 // Default direction no offset
      LayoutDirection.BottomToTop -> remainingSpace //to screen negative
    }
    return offset
  }

  /**
   * Returns the start (x or y depending on the orientation) for the box with the given index
   *
   * ORIGIN is at the *left* or *top*
   *
   * It holds [calculateEnd] - [calculateStart] = [boxSize] if invoked with the same index.
   */
  fun calculateStart(index: BoxIndex): @Zoomed Double {
    return calculateCenter(index) - boxSize / 2.0
  }

  /**
   * Returns the end (x or y depending on the orientation) for the box with the given index
   *
   * ORIGIN is at the *left* or *top*
   *
   * It holds [calculateEnd] - [calculateStart] = [boxSize] if invoked with the same index.
   */
  fun calculateEnd(index: BoxIndex): @Zoomed Double {
    return calculateCenter(index) + boxSize / 2.0
  }

  /**
   * Returns the box index for the given x value
   */
  fun boxIndexFor(position: @px Double): BoxIndex? {
    //the net position without the offset
    @px val netPosition = position - calculateOffset()

    @px val boxSizeAndGap = boxSize + gap
    val index = netPosition / boxSizeAndGap

    @px val gapPart = netPosition % boxSizeAndGap - boxSize

    if (gapPart > 0.0) {
      //Within a gap!
      return null
    }

    val floorIndex = index.toIntFloor()

    if (floorIndex >= numberOfBoxes || floorIndex < 0) {
      return null
    }

    return BoxIndex(floorIndex)
  }

  /**
   * Returns true if this layout does not contain any boxes
   */
  fun isEmpty(): Boolean {
    return numberOfBoxes == 0
  }

  override fun toString(): String {
    return "EquisizedBoxLayout(availableSpace=$availableSpace, numberOfBoxes=$numberOfBoxes, boxSize=$boxSize, gap=$gap, layoutDirection=$layoutDirection)"
  }

  companion object {
    /**
     * Empty layout - without any content
     */
    val empty: EquisizedBoxLayout = EquisizedBoxLayout(0.0, 0, 0.0, 0.0, LayoutDirection.LeftToRight)
  }
}

/**
 * Represents the index of a box within the layout
 */
@JvmInline
value class BoxIndex(val value: Int) {
  companion object {
    val zero: BoxIndex = BoxIndex(0)
    val one: BoxIndex = BoxIndex(1)
  }
}
