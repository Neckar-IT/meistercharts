package com.meistercharts.model

import com.meistercharts.algorithms.axis.AxisSelection
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.number.PositiveOrZero
import it.neckar.open.kotlin.lang.or0ifNaN
import it.neckar.open.kotlin.lang.sqrt
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a distance (x and y values).
 *
 * Values of this class represent a distance between two points. This can be e.g. a vector.
 *
 */
data class Distance(
  val x: @MayBeNegative @MayBeNaN Double,
  val y: @MayBeNegative @MayBeNaN Double
) {

  constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())

  override fun toString(): String {
    return "$x/$y"
  }

  fun multiply(factorX: Double, factorY: Double): Distance {
    return Distance(x * factorX, y + factorY)
  }

  fun divide(divisorX: Double, divisorY: Double): Distance {
    return Distance(x / divisorX, y / divisorY)
  }

  fun withX(newX: @MayBeNaN Double): Distance {
    return Distance(newX, y)
  }

  fun withY(newY: @MayBeNaN Double): Distance {
    return Distance(x, newY)
  }

  fun plus(deltaX: @MayBeNaN Double, deltaY: @MayBeNaN Double): Distance {
    return of(x + deltaX, y + deltaY)
  }

  fun plus(otherDistance: Distance): Distance {
    return of(x + otherDistance.x, y + otherDistance.y)
  }

  fun withMax(maxX: @MayBeNaN Double, maxY: @MayBeNaN Double, axisSelection: AxisSelection = AxisSelection.Both): Distance {
    val newX: Double = if (axisSelection.containsX) {
      min(x, maxX)
    } else {
      x
    }

    val newY = if (axisSelection.containsY) {
      min(y, maxY)
    } else {
      y
    }

    return of(newX, newY)
  }

  fun withMin(minX: @MayBeNaN Double, minY: @MayBeNaN Double, axisSelection: AxisSelection = AxisSelection.Both): Distance {
    val newX = if (axisSelection.containsX) {
      max(x, minX)
    } else {
      x
    }

    val newY = if (axisSelection.containsY) {
      max(y, minY)
    } else {
      y
    }

    return of(newX, newY)
  }

  /**
   * Returns a new instance (if necessary) that replaces NaN with 0.0
   */
  fun avoidNaN(): Distance {
    if (x.isNaN() || y.isNaN()) {
      return Distance(x.or0ifNaN(), y.or0ifNaN())
    }

    return this
  }

  fun isZero(): Boolean {
    return x == 0.0 && y == 0.0
  }

  /**
   * Formats this [Distance] using the given [format]
   */
  fun format(format: CachedNumberFormat = decimalFormat): String {
    return format.format(x) + "/" + format.format(y)
  }

  /**
   * Returns the absolute distance
   */
  fun abs(): Distance {
    if (x >= 0 && y >= 0) {
      return this
    }

    return Distance(x.absoluteValue, y.absoluteValue)
  }

  /**
   * Converts the dimension to a size
   */
  fun asSize(): Size {
    return Size(x, y)
  }

  operator fun minus(other: Distance): Distance {
    return Distance(this.x - other.x, this.y - other.y)
  }

  /**
   * Returns the direct distance (pythagoras)
   * Absolute value only!
   */
  fun direct(): @PositiveOrZero Double {
    return (x * x + y * y).sqrt()
  }

  companion object {
    val zero: Distance = Distance(0.0, 0.0)
    val none: Distance = zero

    fun of(x: Double, y: Double): Distance {
      return Distance(x, y)
    }
  }
}


/**
 * Reinterprets coordinates as distance
 */
fun Coordinates.asDistance(): Distance {
  return Distance(this.x, this.y)
}
