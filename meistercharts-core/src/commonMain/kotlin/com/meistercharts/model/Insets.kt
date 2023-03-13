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
package com.meistercharts.model

import com.meistercharts.annotations.Zoomed
import it.neckar.open.unit.other.px
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmStatic

/**
 * A class that describes space on four sides.
 */
@Serializable
data class Insets(
  val top: Double,
  val right: Double,
  val bottom: Double,
  val left: Double,
) {

  operator fun plus(other: Insets): @Zoomed Insets {
    return Insets(top + other.top, right + other.right, bottom + other.bottom, left + other.left)
  }

  operator fun minus(other: Insets): @Zoomed Insets {
    return Insets(top - other.top, right - other.right, bottom - other.bottom, left - other.left)
  }

  /**
   * Creates a copy of this [Insets] whose top value is changed to [newTop]
   */
  fun withTop(newTop: Double): Insets {
    return Insets(newTop, right, bottom, left)
  }

  /**
   * Creates a copy of this [Insets] whose right value is changed to [newRight]
   */
  fun withRight(newRight: Double): Insets {
    return Insets(top, newRight, bottom, left)
  }

  /**
   * Creates a copy of this [Insets] whose bottom value is changed to [newBottom]
   */
  fun withBottom(newBottom: Double): Insets {
    return Insets(top, right, newBottom, left)
  }

  /**
   * Creates a copy of this [Insets] whose left value is changed to [newLeft]
   */
  fun withLeft(newLeft: Double): Insets {
    return Insets(top, right, bottom, newLeft)
  }

  /**
   * Creates a copy of this [Insets] whose [side] value is changed to [newValue]
   */
  fun withSide(side: Side, newValue: Double): Insets {
    return when (side) {
      Side.Left -> withLeft(newValue)
      Side.Right -> withRight(newValue)
      Side.Top -> withTop(newValue)
      Side.Bottom -> withBottom(newValue)
    }
  }

  operator fun get(side: Side): Double {
    return when (side) {
      Side.Left -> left
      Side.Right -> right
      Side.Top -> top
      Side.Bottom -> bottom
    }
  }

  /**
   * Creates a new instance with only the provided sides
   */
  fun only(
    side0: Side? = null,
    side1: Side? = null,
    side2: Side? = null,
    side3: Side? = null,
  ): Insets {
    val top = if (Side.Top.any(side0, side1, side2, side3)) {
      top
    } else {
      0.0
    }

    val left = if (Side.Left.any(side0, side1, side2, side3)) {
      left
    } else {
      0.0
    }

    val bottom = if (Side.Bottom.any(side0, side1, side2, side3)) {
      bottom
    } else {
      0.0
    }

    val right = if (Side.Right.any(side0, side1, side2, side3)) {
      right
    } else {
      0.0
    }

    return Insets(top, right = right, bottom = bottom, left = left)
  }

  /**
   * Returns the x value - if any of the provided sides equals [Side.Left].
   * Else 0.0 is returned
   */
  fun onlyLeft(
    side0: Side? = null,
    side1: Side? = null,
    side2: Side? = null,
    side3: Side? = null,
  ): Double {
    return if (Side.Left.any(side0, side1, side2, side3)) {
      left
    } else {
      0.0
    }
  }

  fun onlyTop(
    side0: Side? = null,
    side1: Side? = null,
    side2: Side? = null,
    side3: Side? = null,
  ): Double {
    return if (Side.Top.any(side0, side1, side2, side3)) {
      top
    } else {
      0.0
    }
  }

  /**
   * The sum of the space to the right and to the left
   */
  val offsetWidth: Double
    get() = right + left

  /**
   * The sum of the space to the top and to the bottom
   */
  val offsetHeight: Double
    get() = top + bottom

  /**
   * Returns the top left coordinates
   */
  val topLeft: Coordinates
    get() {
      return Coordinates(left, top)
    }

  val topRight: Coordinates
    get() {
      return Coordinates(right, top)
    }

  val bottomLeft: Coordinates
    get() {
      return Coordinates(left, bottom)
    }

  fun plusBottom(additionalBottom: Double): @Zoomed Insets {
    return copy(bottom = bottom + additionalBottom)
  }

  /**
   * Returns a new `Insets` object that contains values for only the selected sides based on the provided `selection` argument.
   *
   * * If the `selection` argument is [SidesSelection.all], then this method returns `this` `Insets` object.
   * * If the `selection` argument is [SidesSelection.none], then this method returns an empty `Insets` object.
   * * Otherwise, this method returns a new `Insets` object that contains the values for the selected sides, and 0 for the unselected sides.
   *
   * @param selection the `SidesSelection` indicating which sides to include in the returned `Insets` object.
   * @return a new `Insets` object with values only for the selected sides, or an empty `Insets` object if [SidesSelection.none] is passed as an argument.
   */
  fun only(selection: SidesSelection): Insets {
    return when (selection) {
      SidesSelection.all -> this
      SidesSelection.none -> empty
      else -> {
        of(
          top = if (selection.topSelected) this.top else 0.0,
          right = if (selection.rightSelected) this.right else 0.0,
          bottom = if (selection.bottomSelected) this.bottom else 0.0,
          left = if (selection.leftSelected) this.left else 0.0
        )
      }
    }
  }

  companion object {
    @JvmStatic
    val empty: Insets = of(0.0)

    @JvmStatic
    val all2: Insets = of(2.0)

    @JvmStatic
    val all5: Insets = of(5.0)

    @JvmStatic
    val all7: Insets = of(7.0)

    @JvmStatic
    val all10: Insets = of(10.0)

    @JvmStatic
    val all15: Insets = of(15.0)

    @JvmStatic
    fun of(allSides: Double): Insets = Insets(allSides, allSides, allSides, allSides)

    @JvmStatic
    fun of(topBottom: Double, rightLeft: Double): Insets = Insets(topBottom, rightLeft, topBottom, rightLeft)

    @JvmStatic
    fun of(top: Double, rightLeft: Double, bottom: Double): Insets = Insets(top, rightLeft, bottom, rightLeft)

    @JvmStatic
    fun of(top: Double, right: Double, bottom: Double, left: Double): Insets = Insets(top, right, bottom, left)

    fun of(top: Int, right: Int, bottom: Int, left: Int): Insets = Insets(top.toDouble(), right.toDouble(), bottom.toDouble(), left.toDouble())

    /**
     * Creates [Insets] where all sides equal 0.0 apart from the top side
     */
    @JvmStatic
    fun onlyTop(top: Double): Insets = Insets(top, 0.0, 0.0, 0.0)

    /**
     * Creates [Insets] where all sides equal 0.0 apart from the right side
     */
    @JvmStatic
    fun onlyRight(right: Double): Insets = Insets(0.0, right, 0.0, 0.0)

    /**
     * Creates [Insets] where all sides equal 0.0 apart from the bottom side
     */
    @JvmStatic
    fun onlyBottom(bottom: Double): Insets = Insets(0.0, 0.0, bottom, 0.0)

    /**
     * Creates [Insets] where all sides equal 0.0 apart from the left side
     */
    @JvmStatic
    fun onlyLeft(left: Double): Insets = Insets(0.0, 0.0, 0.0, left)

    @JvmStatic
    fun topLeft(top: Double, left: Double): Insets = Insets(top, 0.0, 0.0, left)

    @JvmStatic
    fun topRight(top: Double, right: Double): Insets = Insets(top, right, 0.0, 0.0)

    @JvmStatic
    fun bottomLeft(top: Double, left: Double): Insets = Insets(top, 0.0, 0.0, left)

    @JvmStatic
    fun bottomRight(top: Double, right: Double): Insets = Insets(top, right, 0.0, 0.0)

    /**
     * Creates a new instance with the given value for the given side.
     * All other values are set to the default value
     */
    @JvmStatic
    fun only(side: Side, value: @px Double, otherSidesValue: Double = 0.0): Insets {
      return when (side) {
        Side.Left -> Insets(otherSidesValue, otherSidesValue, otherSidesValue, value)
        Side.Right -> Insets(otherSidesValue, value, otherSidesValue, otherSidesValue)
        Side.Top -> Insets(value, otherSidesValue, otherSidesValue, otherSidesValue)
        Side.Bottom -> Insets(otherSidesValue, otherSidesValue, value, otherSidesValue)
      }
    }

    /**
     * Creates insets in the given direction
     */
    fun create(direction: Direction, size: @px Double): Insets {
      return when (direction) {
        Direction.Center -> empty
        Direction.CenterLeft -> onlyLeft(size)
        Direction.CenterRight -> onlyRight(size)
        Direction.BaseLineCenter -> empty
        Direction.BaseLineLeft -> onlyLeft(size)
        Direction.BaseLineRight -> onlyRight(size)
        Direction.TopLeft -> topLeft(size, size)
        Direction.TopCenter -> onlyTop(size)
        Direction.TopRight -> topRight(size, size)
        Direction.BottomLeft -> bottomLeft(size, size)
        Direction.BottomCenter -> onlyBottom(size)
        Direction.BottomRight -> bottomRight(size, size)
      }
    }
  }
}
