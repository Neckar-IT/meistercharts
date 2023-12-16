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
package it.neckar.geometry

import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.intFormat
import it.neckar.open.kotlin.lang.normalize
import it.neckar.open.kotlin.lang.or0ifNaN
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.pct
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a size (width and height values).
 */
@Serializable
data class Size(
  val width: Double,
  val height: Double,
) {

  constructor(width: Int, height: Int) : this(width.toDouble(), height.toDouble())

  /**
   * Returns the aspect ratio (width/height)
   */
  val aspectRatio: Double
    get() {
      return width / height
    }

  override fun toString(): String {
    return "${width}x$height"
  }

  fun withWidth(newWidth: Double): Size {
    return Size(newWidth, height)
  }

  fun withHeight(newHeight: Double): Size {
    return Size(width, newHeight)
  }

  fun plus(deltaWidth: Double, deltaHeight: Double): Size {
    return of(width + deltaWidth, height + deltaHeight)
  }

  operator fun plus(distance: Distance): Size {
    return of(width + distance.x, height + distance.y)
  }

  fun withMax(maxWidth: Double, maxHeight: Double): Size {
    return of(min(width, maxWidth), min(height, maxHeight))
  }

  fun withMin(minWidth: Double, minHeight: Double): Size {
    return of(max(width, minWidth), max(height, minHeight))
  }

  /**
   * Returns a size with the width/height applied from the given size
   */
  fun with(size: Size, axisSelection: AxisSelection): Size {
    if (axisSelection == AxisSelection.None) {
      return this
    }

    if (axisSelection == AxisSelection.Both) {
      return size
    }

    return with(size.width, size.height, axisSelection)
  }

  /**
   * Returns a size with the given width/height
   */
  fun with(width: Double, height: Double, axisSelection: AxisSelection): Size {
    val newWidth = if (axisSelection.containsX) width else this.width
    val newHeight = if (axisSelection.containsY) height else this.height
    return Size(newWidth, newHeight)
  }

  /**
   * Multiplies width and height with the given factor
   */
  fun times(factor: Double): Size {
    if (factor == 1.0) {
      return this
    }

    return Size(width * factor, height * factor)
  }

  fun times(scaleWidth: Double, scaleHeight: Double): Size {
    if (scaleWidth == 1.0 && scaleHeight == 1.0) {
      return this
    }

    return of(width * scaleWidth, height * scaleHeight)
  }

  fun divide(divisor: Double): Size {
    if (divisor == 1.0) {
      return this
    }

    return Size(width / divisor, height / divisor)
  }

  /**
   * Returns true if both width and height are 0.0
   */
  fun isZero(): Boolean {
    return width == 0.0 && height == 0.0
  }

  /**
   * Returns a new instance (if necessary) that replaces NaN with 0.0
   */
  fun avoidNaN(): Size {
    if (width.isNaN() || height.isNaN()) {
      return Size(width.or0ifNaN(), height.or0ifNaN())
    }

    return this
  }

  /**
   * Returns true if the width and height are both smaller than the width/height of the given size
   */
  fun bothSmallerThan(size: Size): Boolean {
    return width < size.width && height < size.height
  }

  fun bothSmallerThanOrEqual(size: Size): Boolean {
    return width <= size.width && height <= size.height
  }

  fun bothSmallerThanOrEqual(width: Double, height: Double): Boolean {
    return this.width <= width && this.height <= height
  }

  fun bothLargerThanOrEqual(width: Double, height: Double): Boolean {
    return this.width >= width && this.height >= height
  }

  fun bothLargerThan(size: Size): Boolean {
    return width > size.width && height > size.height
  }

  /**
   * Returns true if both values are larger than the given value
   */
  fun bothLargerThan(value: Double): Boolean {
    return width > value && height > value
  }

  /**
   * Returns true if both values are zero or positive
   */
  fun bothNotNegative(): Boolean {
    return width >= 0.0 && height >= 0.0
  }

  /**
   * Returns true if at least one of the values is smaller than the given size
   */
  fun atLeastOneSmallerThan(size: Size): Boolean {
    return width < size.width || height < size.height
  }

  /**
   * Returns true if at least one of the values is greater than the given size
   */
  fun atLeastOneGreaterThan(size: Size): Boolean {
    return width > size.width || height > size.height
  }

  /**
   * Returns true if at least one of the values is 0.0
   */
  fun atLeastOneZero(): Boolean {
    return width == 0.0 || height == 0.0
  }

  /**
   * Returns a new size with substracted values
   */
  fun minus(minusWidth: Double, minusHeight: Double): Size {
    if (minusWidth == 0.0 && minusHeight == 0.0) {
      return this
    }

    return of(width - minusWidth, height - minusHeight)
  }

  /**
   * Returns this size as coordinate relative to the given start point.
   *
   * This method returns the translation of this from the given coordinate.
   */
  fun toCoordinate(startPoint: Coordinates = Coordinates.origin): Coordinates {
    return Coordinates.of(startPoint.x + width, startPoint.y + height)
  }

  /**
   * Returns the center of the size as coordinates
   */
  fun center(): Coordinates {
    return Coordinates(this.width / 2.0, this.height / 2.0)
  }

  /**
   * Returns a new size that is same or smaller than this with the given aspect ratio
   */
  fun fitWithAspectRatio(aspectRatio: Double): Size {
    val maxWidth = height * aspectRatio
    val maxHeight = width / aspectRatio

    return Size(width.coerceAtMost(maxWidth), height.coerceAtMost(maxHeight))
  }

  /**
   * Returns a new size that is the same or *larger* than this with the given aspect ratio
   */
  fun containWithAspectRatio(aspectRatio: Double): Size {
    val minWidth = height * aspectRatio
    val minHeight = width / aspectRatio

    return Size(width.coerceAtLeast(minWidth), height.coerceAtLeast(minHeight))
  }

  fun format(format: NumberFormat = intFormat): String {
    return "${format.format(width)} mm x ${format.format(height)} mm"
  }

  fun coerceAtLeast(minimum: Size): Size {
    return of(
      width.coerceAtLeast(minimum.width),
      height.coerceAtLeast(minimum.height)
    )
  }

  fun coerceAtMost(maximum: Size): Size {
    return of(
      width.coerceAtMost(maximum.width),
      height.coerceAtMost(maximum.height)
    )
  }

  fun coerceIn(minimum: Size, maximum: Size): Size {
    return of(
      width.coerceIn(minimum.width, maximum.width),
      height.coerceIn(minimum.height, maximum.height)
    )
  }

  /**
   * Creates a new [Size] whose width is [newWidth] and whose height is scaled accordingly, i. e. the aspect ratio is kept.
   */
  fun scaleToWidth(newWidth: Double): Size {
    if (this.width == 0.0) {
      return invalid
    }
    val scale = newWidth / this.width
    return times(scale)
  }

  fun scaleToMax(maxWidth: Double, maxHeight: Double): Size {
    if (this.width == 0.0) {
      return invalid
    }
    if (this.height == 0.0) {
      return invalid
    }

    // if the size is already smaller than the max size, return this
    if (this.width <= maxWidth && this.height <= maxHeight) {
      return this
    }

    return if (this.aspectRatio > maxWidth / maxHeight) {
      scaleToWidth(maxWidth)
    } else {
      scaleToHeight(maxHeight)
    }
  }

  /**
   * Creates a new [Size] whose height is [newHeight] and whose width is scaled accordingly, i. e. the aspect ratio is kept.
   */
  fun scaleToHeight(newHeight: Double): Size {
    if (this.height == 0.0) {
      return invalid
    }
    val scale = newHeight / this.height
    return times(scale)
  }

  /**
   * Normalizes the size.
   *
   * Returns this size as percentage of the base size
   */
  fun normalize(base: Size): @pct Size {
    return of(
      width.normalize(base.width),
      height.normalize(base.height)
    )
  }

  /**
   * Calculates the area for this size
   */
  fun area(): Double {
    return width * height
  }

  /**
   * Returns true if this has the provided width and height
   */
  fun isEqualTo(width: Double, height: Double): Boolean {
    return this.width == width && this.height == height
  }

  fun isFinite(): Boolean {
    return width.isFinite() && height.isFinite()
  }

  companion object {
    val NaN: @MayBeNaN Size = Size(Double.NaN, Double.NaN)
    val invalid: @MayBeNaN Size = NaN

    val zero: Size = Size(0.0, 0.0)
    val none: Size = zero
    val one: Size = Size(1.0, 1.0)
    val half: Size = Size(0.5, 0.5)

    val PX_16: Size = both(16.0)
    val PX_24: Size = both(24.0)
    val PX_30: Size = both(30.0)
    val PX_40: Size = both(40.0)
    val PX_50: Size = both(50.0)
    val PX_60: Size = both(60.0)
    val PX_90: Size = both(90.0)
    val PX_120: Size = both(120.0)

    val FullHD: Size = Size(1920.0, 1080.0)

    fun of(width: Double, height: Double): Size {
      return Size(width, height)
    }

    fun of(width: Int, height: Int): Size {
      return of(width.toDouble(), height.toDouble())
    }

    /**
     * Returns a size with width and height set to the same value
     */
    fun both(widthHeight: Double): Size {
      return Size(widthHeight, widthHeight)
    }
  }
}
