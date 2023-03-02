package com.meistercharts.model

import it.neckar.open.unit.number.MayBeNaN
import com.meistercharts.annotations.Zoomed
import it.neckar.open.kotlin.lang.betweenInclusive
import it.neckar.open.kotlin.lang.distance
import it.neckar.open.kotlin.lang.isPositive
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.mm
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Represents a coordinate
 *
 */
@Serializable
data class Coordinates(
  val x: Double,
  val y: Double
) {
  constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())

  fun plus(deltaX: Double, deltaY: Double): Coordinates {
    return of(x + deltaX, y + deltaY)
  }

  fun coerceAtLeast(minimum: Coordinates): Coordinates {
    return of(
      x.coerceAtLeast(minimum.x),
      y.coerceAtLeast(minimum.y)
    )
  }

  fun coerceAtMost(maximum: Coordinates): Coordinates {
    return of(
      x.coerceAtMost(maximum.x),
      y.coerceAtMost(maximum.y)
    )
  }

  fun coerceIn(minimum: Coordinates, maximumX: Double, maximumY: Double): Coordinates {
    return of(
      x.coerceIn(minimum.x, maximumX),
      y.coerceIn(minimum.y, maximumY)
    )
  }

  /**
   * Adds the given vector and returns the resulting coordinate
   */
  operator fun plus(vector: Distance): Coordinates {
    if (vector.isZero()) {
      return this
    }

    return of(x + vector.x, y + vector.y)
  }

  /**
   * Adds the given size and returns the resulting coordinate
   */
  fun plus(size: Size): Coordinates {
    return of(x + size.width, y + size.height)
  }

  fun withMax(maxX: Double, maxY: Double): Coordinates {
    return of(min(x, maxX), min(y, maxY))
  }

  fun withMin(minX: Double, minY: Double): Coordinates {
    return of(max(x, minX), max(y, minY))
  }

  fun withX(value: Double): Coordinates {
    return of(value, y)
  }

  fun withY(value: Double): Coordinates {
    return of(x, value)
  }

  override fun toString(): String {
    return "$x/$y"
  }

  /**
   * Calculates the distance from the given coordinate to this coordinate
   */
  fun delta(other: Coordinates): Distance {
    return Distance.of(x - other.x, y - other.y)
  }

  /**
   * Calculates the distance from the given coordinate to this coordinate.
   *
   * The returned distance is guaranteed to have positive values.
   */
  fun deltaAbsolute(other: Coordinates): Distance {
    return Distance.of(abs(x - other.x), abs(y - other.y))
  }

  /**
   * Subtracts the given coordinate from this
   */
  operator fun minus(other: Distance): Coordinates {
    return minus(other.x, other.y)
  }

  fun minus(deltaX: Double, deltaY: Double): Coordinates {
    return of(x - deltaX, y - deltaY)
  }

  /**
   * Calculates the delta between two coordinates
   */
  operator fun minus(other: Coordinates): Distance {
    return Distance.of(x - other.x, y - other.y)
  }

  /**
   * Subtracts the given coordinate from this.
   *
   * Attention: Results the result as coordinates.
   * This is probably not the method you are looking for!
   */
  fun minusAsCoordinates(other: Coordinates): Coordinates {
    return of(x - other.x, y - other.y)
  }

  operator fun plus(that: Coordinates): Coordinates = Coordinates(x + that.x, y + that.y)

  operator fun times(that: Coordinates): Coordinates = Coordinates(x * that.x, y * that.y)
  operator fun times(scale: Double): Coordinates = Coordinates(x * scale, y * scale)
  fun times(scaleX: Double, scaleY: Double): Coordinates = Coordinates(x * scaleX, y * scaleY)

  operator fun div(that: Coordinates): Coordinates = Coordinates(x / that.x, y / that.y)
  operator fun div(scale: Double): Coordinates = Coordinates(x / scale, y / scale)


  /**
   * Computes the center between these [Coordinates] and [other]
   */
  fun center(other: Coordinates): Coordinates {
    return Coordinates(
      0.5 * (this.x + other.x),
      0.5 * (this.y + other.y)
    )
  }

  /**
   *  Computes [PolarCoordinates] from [Coordinates].
   *  @see <a href="https://en.wikipedia.org/wiki/Polar_coordinate_system#Converting_between_polar_and_Cartesian_coordinates">Wikipedia</a>
   */
  fun toPolar(): PolarCoordinates {
    return PolarCoordinates(sqrt(x * x + y * y), atan2(y, x))
  }

  /**
   * Formats the coordinates
   */
  fun format(format: CachedNumberFormat = decimalFormat): String {
    return "${format.format(x)}/${format.format(y)}"
  }

  /**
   * Returns true if this coordinate is close to the given coordinate.
   */
  fun isCloseTo(other: Coordinates, deltaX: Double, deltaY: Double = deltaX): Boolean {
    return x.betweenInclusive(other.x - deltaX, other.x + deltaX)
      &&
      y.betweenInclusive(other.y - deltaY, other.y + deltaY)
  }

  /**
   * Normalizes the coordinates.
   *
   * Returns this size as percentage of the base size
   */
  fun normalize(base: Size): @pct Coordinates {
    return of(
      1.0 / base.width * x,
      1.0 / base.height * y,
    )
  }

  /**
   * Calculates the distance from this and the other coordinates
   */
  fun distanceTo(target: Coordinates): Double {
    return distanceTo(target.x, target.y)
  }

  fun distanceTo(targetX: Double, targetY: Double): Double {
    return distance(x, y, targetX, targetY)
  }

  /**
   * Returns the coordinates % the given values
   */
  fun mod(modX: Double, modY: @mm Double): Coordinates {
    require(modX.isPositive()) { "Invalid modX: $modX" }
    require(modY.isPositive()) { "Invalid modY: $modY" }

    return (Coordinates(x % modX, y % modY))
  }

  fun isFinite(): Boolean {
    return x.isFinite() && y.isFinite()
  }


  companion object {
    @JvmField
    val origin: Coordinates = Coordinates(0, 0)

    /**
     * No translation at all
     */
    @JvmField
    val none: Coordinates = origin

    @JvmField
    val NaN: @MayBeNaN Coordinates = Coordinates(Double.NaN, Double.NaN)
    val invalid: @MayBeNaN Coordinates = NaN

    @JvmStatic
    fun of(x: Double, y: Double): Coordinates {
      return Coordinates(x, y)
    }

    /**
     * Returns the min of x an y of both coordinates
     */
    @JvmStatic
    fun minOf(first: Coordinates, second: Coordinates): Coordinates {
      return of(min(first.x, second.x), min(first.y, second.y))
    }

    /**
     * Returns the max of x and y of both coordinates
     */
    @JvmStatic
    fun maxOf(first: Coordinates, second: Coordinates): Coordinates {
      return of(max(first.x, second.x), max(first.y, second.y))
    }

    /**
     * Calculates the center between two coordinates
     */
    @JvmStatic
    fun center(first: Coordinates, second: Coordinates): Coordinates {
      return first.center(second)
    }
  }
}

/**
 * Returns true if this coordinates lies within a rectangle defined by the given location and size
 */
fun Coordinates.within(location: Coordinates, size: @Zoomed Size): Boolean {
  return x.betweenInclusive(location.x, location.x + size.width)
    &&
    y.betweenInclusive(location.y, location.y + size.height)
}
