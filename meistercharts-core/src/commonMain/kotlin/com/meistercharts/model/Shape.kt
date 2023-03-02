package com.meistercharts.model

import it.neckar.open.unit.number.MayBeNegative
import com.meistercharts.provider.Box
import it.neckar.open.unit.si.rad
import kotlin.math.tan

/**
 * Represents a rectangle.
 *
 * The rectangle has a size that starts at a given location. The size may be negative.
 * Therefore, the upper left corner of the rectangle is:
 * * the location if the size is positive
 * * location - size if the size is negative
 */
interface Shape : Box {

  val location: Coordinates

  @MayBeNegative
  val size: Size

  override fun getX(): Double = location.x

  override fun getY(): Double = location.y

  /**
   * The width which might be negative
   */
  @MayBeNegative
  override fun getWidth(): Double = size.width

  /**
   * The height which might be negative
   */
  @MayBeNegative
  override fun getHeight(): Double = size.height

  fun vertices(): List<Coordinates>

  fun contains(coordinates: Coordinates): Boolean {
    return contains(coordinates.x, coordinates.y)
  }

  fun contains(targetX: Double, targetY: Double): Boolean {
    return containsX(targetX) && containsY(targetY)
  }

  fun containsX(targetX: Double): Boolean = targetX in left..right

  fun containsY(targetY: Double): Boolean = targetY in top..bottom

  /**
   * Returns the x value for the given horizontal alignment
   */
  fun x(alignment: HorizontalAlignment): Double {
    return when (alignment) {
      HorizontalAlignment.Left -> left
      HorizontalAlignment.Right -> right
      HorizontalAlignment.Center -> centerX
    }
  }

  fun y(alignment: VerticalAlignment): Double {
    return when (alignment) {
      VerticalAlignment.Top -> top
      VerticalAlignment.Center -> centerY
      VerticalAlignment.Baseline -> centerY //TODO(?)
      VerticalAlignment.Bottom -> bottom
    }
  }

  /**
   * Moves the rectangle
   */
  fun move(deltaX: Double, deltaY: Double): Shape

  fun move(distance: Distance): Shape {
    return move(distance.x, distance.y)
  }

  /**
   * Returns the x value (relative to the rect) for this rectangle when following the given angle from origin.
   *
   * This method calculates the intersection (x value) between this rectangle and an (imaginary) line from origin 0/0 with the given angle.
   */
  fun xFromRadRelative(theta: @rad Double): Double {
    return if (PolarCoordinates.isToTheTop(theta)) {
      top / tan(theta)
    } else {
      bottom / tan(theta)
    }.coerceIn(left, right)
  }

  /**
   * Returns the y value (relative to the rect) for this rectangle box when following the given angle from origin
   *
   * This method calculates the intersection (y value) between this rectangle and an (imaginary) line from origin 0/0 with the given angle.
   */
  fun yFromRadRelative(theta: @rad Double): Double {
    return if (PolarCoordinates.isToTheRight(theta)) {
      (tan(theta) * right)
    } else {
      tan(theta) * left
    }.coerceIn(top, bottom)
  }

  fun withX(newX: Double): Shape

  fun withY(newY: Double): Shape

  /**
   * Creates a [Rectangle] from this [Rectangle] with width [newWidth]
   */
  fun withWidth(newWidth: Double): Shape

  /**
   * Creates a [Rectangle] from this [Rectangle] with height [newHeight]
   */
  fun withHeight(newHeight: Double): Shape

  /**
   * Returns a new rectangle object with the given location - but the same height/width
   */
  fun withLocation(location: Coordinates): Shape

  /**
   * Returns a new rectangle that has been extended with the given values
   */
  fun expand(left: Double = 0.0, top: Double = 0.0, right: Double = 0.0, bottom: Double = 0.0): Shape

  /**
   * Returns true if this rectangle overlaps with another rectangle
   */
  fun overlaps(other: Shape): Boolean {
    return !doesNotOverlap(other)
  }

  fun doesNotOverlap(other: Shape): Boolean {
    return doesNotOverlapBox(other)
  }
}
