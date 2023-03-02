package com.meistercharts.canvas

import com.meistercharts.algorithms.environment
import it.neckar.open.unit.number.Integer
import it.neckar.open.kotlin.lang.ceil
import it.neckar.open.kotlin.lang.isEven
import it.neckar.open.kotlin.lang.isOdd
import it.neckar.open.kotlin.lang.round
import it.neckar.open.unit.other.px
import kotlin.math.ceil

/**
 * Painting utils
 */
object PaintingUtils {

  /**
   * Rounds the given value if snap to pixel is set to true
   */
  @px
  fun snapPosition(@px value: Double, snapToPixel: Boolean): Double {
    return snapPositionFactor(value, snapToPixel, 1 / environment.devicePixelRatio)
  }

  /**
   * Returns the rounded down value if snapToPixel is set to true
   */
  @px
  fun snapSize(@px value: Double, snapToPixel: Boolean): Double {
    if (!snapToPixel) {
      return value
    }

    return snapSizeFactor(value, snapToPixel, 1 / environment.devicePixelRatio)
  }

  /**
   * Returns the rounded down value if snapToPixel is set to true
   */
  @px
  @Deprecated("probably no longer required")
  fun snapSize(@px value: Double, snapToPixel: Boolean, snapMode: SnapMode = SnapMode.EVEN_ODD): Double {
    if (!snapToPixel) {
      return value
    }

    return snapMode.increaseIfNecessary(ceil(value))
  }

  /**
   * Snaps a size to a multiple of a factor (ceil!)
   */
  fun snapSizeFactor(size: Double, snap: Boolean, factor: Double): Double {
    if (!snap) {
      return size
    }

    return (size / factor).ceil() * factor
  }

  /**
   * Snaps a position to a multiple of a factor (rounds the value)
   */
  fun snapPositionFactor(size: Double, snap: Boolean, factor: Double): Double {
    if (!snap) {
      return size
    }

    return (size / factor).round() * factor
  }
}

/**
 * The snap mode
 */
@Deprecated("probably no longer required")
enum class SnapMode {
  /**
   * Snaps to both even and odd values
   */
  EVEN_ODD,

  /**
   * Snap only to even numbers
   */
  EVEN,

  /**
   * Snap only to odd numbers
   */
  ODD;

  /**
   * Increases the given value (if necessary).
   *
   * ATTENTION: Only works for "Integer" values
   */
  fun increaseIfNecessary(value: @Integer Double): Double {
    return when (this) {
      EVEN_ODD -> value
      EVEN -> if (value.isOdd()) value + 1.0 else value
      ODD -> if (value.isEven()) value + 1.0 else value
    }
  }
}
