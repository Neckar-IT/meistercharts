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

import it.neckar.open.unit.number.Positive
import it.neckar.open.kotlin.lang.isPositive
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import kotlin.jvm.JvmStatic
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a pair of zoom for both axis
 *
 * * Values greater than 1.0 generally indicate a zoomed in state
 * * Values smaller than 1.0 generally indicate a zoomed out state
 *
 */
data class Zoom(
  val scaleX: @Positive Double = 1.0,
  val scaleY: @Positive Double = 1.0
) {

  init {
    require(scaleX.isPositive() && !scaleX.isInfinite()) { "Scale x must be positive but was <$scaleX>" }
    require(scaleY.isPositive() && !scaleY.isInfinite()) { "Scale y must be positive but was <$scaleY>" }
  }

  /**
   * Returns the aspect ratio (scaleX/scaleY)
   */
  val aspectRatio: Double
    get() {
      return scaleX / scaleY
    }

  override fun toString(): String {
    return "scaleX: $scaleX; scaleY: $scaleY"
  }

  /**
   * Creates new zoom and ensures the minimum is respected
   */
  fun withMin(minScaleXY: @Positive Double): Zoom {
    return withMin(minScaleXY, minScaleXY)
  }

  fun withMin(minScaleX: @Positive Double, minScaleY: @Positive Double): Zoom {
    if (minScaleX <= scaleX && minScaleY <= scaleY) {
      return this
    }

    return of(max(minScaleX, scaleX), max(minScaleY, scaleY))
  }

  /**
   * Returns a new zoom object with the given min zoom for x
   */
  fun withMinX(minScaleX: @Positive Double): Zoom {
    if (minScaleX <= scaleX) {
      return this
    }

    return of(max(minScaleX, scaleX), scaleY)
  }

  fun withMax(maxScaleXY: @Positive Double): Zoom {
    return withMax(maxScaleXY, maxScaleXY)
  }

  fun withMax(maxScaleX: @Positive Double, maxScaleY: @Positive Double): Zoom {
    if (maxScaleX >= scaleX && maxScaleY >= scaleY) {
      return this
    }

    return of(min(maxScaleX, scaleX), min(maxScaleY, scaleY))
  }

  fun withX(updatedZoomScaleX: @Positive Double): Zoom {
    if (updatedZoomScaleX == this.scaleX) {
      return this
    }

    return Zoom(updatedZoomScaleX, scaleY)
  }

  fun withY(updatedZoomScaleY: @Positive Double): Zoom {
    if (updatedZoomScaleY == this.scaleY) {
      return this
    }

    return Zoom(scaleX, updatedZoomScaleY)
  }

  fun format(format: CachedNumberFormat = decimalFormat): String {
    return "${format.format(scaleX)}/${format.format(scaleY)}"
  }

  fun multiply(x: @Positive Double, y: @Positive Double): Zoom {
    if (x == 1.0 && y == 1.0) {
      return this
    }

    return Zoom(this.scaleX * x, this.scaleY * y)
  }

  /**
   * Returns a new zoom object that has the same values for both axis
   */
  fun smallerValueForBoth(): Zoom {
    if (scaleX == scaleY) {
      return this
    }

    val factor = min(scaleX, scaleY)
    return Zoom(factor, factor)
  }

  companion object {
    @JvmStatic
    val default: Zoom = Zoom(1.0, 1.0)

    fun of(zoomScaleX: Double, zoomScaleY: Double): Zoom {
      return Zoom(zoomScaleX, zoomScaleY)
    }
  }
}
