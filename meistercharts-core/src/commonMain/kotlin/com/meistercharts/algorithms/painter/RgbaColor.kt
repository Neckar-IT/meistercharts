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
package com.meistercharts.algorithms.painter

import it.neckar.open.collections.cache
import it.neckar.open.unit.other.pct
import kotlin.math.roundToInt

/**
 * A flat color with RGB and alpha values.
 *
 * Represents a color in the RGBA color space.
 */
data class RgbaColor(
  /**
   * Red (0..225)
   */
  val red: Int,
  /**
   * Green (0..225)
   */
  val green: Int,
  /**
   * Blue (0..225)
   */
  val blue: Int,
  /**
   * The alpha (from 0 which is completely transparent to 1 which is completely opaque)
   */
  val alpha: @pct Double = 1.0,
) : Color {
  init {
    require((0..255).contains(red)) { "Red out of range: <${red}>. Allowed values between 0 and 255" }
    require((0..255).contains(green)) { "Green out of range: <${green}>. Allowed values between 0 and 255" }
    require((0..255).contains(blue)) { "Blue out of range: <${blue}>. Allowed values between 0 and 255" }

    require((0.0..1.0).contains(alpha)) { "Alpha out of range: <$alpha>. Allowed values between 0.0 and 1.0" }
  }

  override val web: String = Colors.asWebString(red, green, blue, alpha)

  /**
   * Returns a rgba string
   */
  fun formatRgba(): String {
    return Colors.toRgbaString(red, green, blue, alpha)
  }

  override fun toCanvasPaint(x0: Double, y0: Double, x1: Double, y1: Double): CanvasPaint {
    return this
  }

  /**
   * Converts the color to a float value that can be used to sort colors
   */
  fun sortableValue(): Double {
    return (red * 0.299 + green * 0.587 + blue * 0.114) / 256
  }

  /**
   * Returns a tint of this color (adds white)
   */
  fun lighter(tintFactor: @pct Double): RgbaColor {
    return RgbaColor(lighter(red, tintFactor), lighter(green, tintFactor), lighter(blue, tintFactor), alpha)
  }

  /**
   * Returns a shade of this color (adds black)
   */
  fun darker(shadeFactor: @pct Double): RgbaColor {
    return RgbaColor(darker(red, shadeFactor), darker(green, shadeFactor), darker(blue, shadeFactor), alpha)
  }

  /**
   * With alpha
   */
  fun withAlpha(newAlpha: @pct Double): RgbaColor {
    val key = 31 * newAlpha.hashCode() + 17 * this.hashCode()

    return alphaColorsCache.getOrStore(key) {
      RgbaColor(red, green, blue, newAlpha)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Color) return false

    return web == other.web
  }

  override fun hashCode(): Int {
    return web.hashCode()
  }

  override fun toString(): String {
    return web
  }

  companion object {
    /**
     * Contains the cached alpha colors - extract from a color
     */
    private val alphaColorsCache = cache<Int, RgbaColor>(description = "alphaColorsCache", maxSize = 100)

    /**
     * Creates a new rgb color using double values (0.0..1.0)
     */
    operator fun invoke(
      /**
       * Red (0..1)
       */
      red: @pct Double,
      /**
       * Green (0..1)
       */
      green: @pct Double,
      /**
       * Blue (0..1)
       */
      blue: @pct Double,
      /**
       * Alpha (0..1)
       */
      alpha: @pct Double = 1.0,
    ): RgbaColor {
      require((0.0..1.0).contains(red)) { "Red out of range: <$red>. Allowed values between 0 and 1.0" }
      require((0.0..1.0).contains(green)) { "Green out of range: <$green>. Allowed values between 0 and 1.0" }
      require((0.0..1.0).contains(blue)) { "Blue out of range: <$blue>. Allowed values between 0 and 1.0" }

      return RgbaColor(
        (red * 255.0).roundToInt(),
        (green * 255.0).roundToInt(),
        (blue * 255.0).roundToInt(),
        alpha
      )
    }

    private fun lighter(colorValue: Int, tintFactor: @pct Double): Int {
      return (colorValue + (255 - colorValue) * tintFactor).coerceAtMost(255.0).roundToInt()
    }

    private fun darker(colorValue: Int, shadeFactor: @pct Double): Int {
      return (colorValue * (1.0 - shadeFactor)).coerceAtLeast(0.0).roundToInt()
    }
  }
}
