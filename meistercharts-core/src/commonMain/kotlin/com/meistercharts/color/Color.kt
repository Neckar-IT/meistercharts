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
package com.meistercharts.color

import com.meistercharts.color.Colors.parse2DigitHex
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.unit.other.pct
import kotlin.jvm.JvmField

/**
 * Provides a color
 */
typealias ColorProvider = () -> Color
/**
 * Maps a color to another color
 */
typealias ColorMapper = (Color) -> Color
/**
 * Maps a color to another color.
 * Might return null if no mapping is possible
 */
typealias ColorMapperNullable = (Color?) -> Color?

/**
 * Does potentially provide a color - or not.
 * Might be null *itself*.
 */
typealias ColorProviderNullable = (() -> Color?)?

/**
 * Extension method to get the color
 */
inline fun ColorProviderNullable.get(): Color? {
  return this?.invoke()
}

typealias RgbaColorProvider = () -> RgbaColor

/**
 * Represents a flat color.
 *
 * ATTENTION: [equals] and [hashCode] use *only* the [web] representation of the color.
 * There are no other checks.
 *
 * Therefore, different subclasses that result in the same web string are equal.
 * But different web strings that will represent the same color are *not* equal.
 */
sealed interface Color : CanvasPaint, CanvasPaintProvider {
  /**
   * Returns a web string that represents this color.
   *
   * This could either be a hex string or a rgba string or some other form of color representation.
   */
  val web: String

  /**
   * Converts this color to a [RgbaColor].
   * This method parses the [web] string if necessary.
   */
  fun toRgba(): RgbaColor

  companion object {
    /**
     * Creates a color from a web string.
     */
    operator fun invoke(web: String): UnparsedWebColor {
      return web(web)
    }

    /**
     * Creates a color from a web string.
     *
     * Does *not* parse the color. Call [Color.toRgba] to parse the color.
     */
    fun web(web: String): UnparsedWebColor {
      return unparsed(web)
    }

    /**
     * Creates an unparsed color from a web string.
     */
    fun unparsed(web: String): UnparsedWebColor {
      return UnparsedWebColor(web)
    }

    /**
     * Creates a rgba color
     */
    fun rgba(
      /**
       * Red (0..225)
       */
      red: Int,
      /**
       * Green (0..225)
       */
      green: Int,
      /**
       * Blue (0..225)
       */
      blue: Int,
      /**
       * The alpha (from 0 which is completely transparent to 1 which is completely opaque)
       */
      alpha: @pct Double = 1.0,
    ): RgbaColor {
      return RgbaColor(red, green, blue, alpha)
    }

    fun rgb(
      /**
       * Red (0..225)
       */
      red: Int,
      /**
       * Green (0..225)
       */
      green: Int,
      /**
       * Blue (0..225)
       */
      blue: Int,
    ): RgbaColor {
      return RgbaColor(red, green, blue, 1.0)
    }

    /**
     * Parses a hex string (#231122) consisting of 6 digits (or 8 digits if alpha is included)
     */
    fun parseHex(web: String): RgbaColor {
      require(web.startsWith("#")) { "Hex string must start with # but was <$web>" }

      val hex = web.removePrefix("#")

      when (hex.length) {
        6 -> {
          val red = hex.substring(0, 2).parse2DigitHex()
          val green = hex.substring(2, 4).parse2DigitHex()
          val blue = hex.substring(4, 6).parse2DigitHex()

          return RgbaColor(red, green, blue)
        }

        8 -> {
          val red = hex.substring(0, 2).parse2DigitHex()
          val green = hex.substring(2, 4).parse2DigitHex()
          val blue = hex.substring(4, 6).parse2DigitHex()
          val alpha = hex.substring(6, 8).parse2DigitHex() / 255.0

          return RgbaColor(red, green, blue, alpha)
        }

        else -> throw IllegalArgumentException("Only hex strings in the format #112233 are supported but was <$web>")
      }
    }

    /**
     * Parses a rgb string (e.g. "rgb(177,210,37)")
     */
    fun parseRgb(rgbValue: String): RgbaColor {
      val rgb = rgbValue
        .removePrefix("rgb(")
        .removeSuffix(")")
        .split(",")

      require(rgb.size == 3) { "Only rgb strings in the format rgb(1,2,3) are supported but was <$rgbValue>" }

      val red = rgb[0].trim().toInt()
      val green = rgb[1].trim().toInt()
      val blue = rgb[2].trim().toInt()

      return RgbaColor(red, green, blue)
    }

    /**
     * Parses a rgba string (e.g. "rgba(144,236,55,0.5)")
     */
    fun parseRgba(rgbaValue: String): RgbaColor {
      val rgba = rgbaValue.removePrefix("rgba(").removeSuffix(")").split(",")

      require(rgba.size == 4) { "Only rgba strings in the format rgba(1,2,3,4) are supported but was <$rgbaValue>" }

      val red = rgba[0].trim().toInt()
      val green = rgba[1].trim().toInt()
      val blue = rgba[2].trim().toInt()
      val alpha = rgba[3].trim().toDouble()

      return RgbaColor(red, green, blue, alpha)
    }

    /**
     * Creates a color with red, green, and blue values in the range of 0 to 1, and an optional opacity value in the
     * range of 0 to 1.
     *
     * @param red The red value of the color, in the range of 0 to 1.
     * @param green The green value of the color, in the range of 0 to 1.
     * @param blue The blue value of the color, in the range of 0 to 1.
     * @param opacity An optional opacity value for the color, in the range of 0 to 1. If not specified, the color will be fully opaque.
     *
     * @return An RgbaColor object representing the specified color and opacity.
     */
    fun color(red: Double, green: Double, blue: Double, opacity: Double = 1.0): RgbaColor {
      return RgbaColor(red, green, blue, opacity)
    }

    /**
     * Creates a fully opaque, random color
     */
    fun random(): RgbaColor {
      val red = it.neckar.open.kotlin.lang.random.nextInt(256)
      val green = it.neckar.open.kotlin.lang.random.nextInt(256)
      val blue = it.neckar.open.kotlin.lang.random.nextInt(256)
      return RgbaColor(red, green, blue)
    }

    /**
     * Parses hex and rgba strings
     */
    fun parseHexOrRgba(hexOrRgba: String): RgbaColor {
      return ColorConversionCache.parseHexOrRgbaCache.getOrStore(hexOrRgba) {
        return when {
          hexOrRgba.startsWith("#") -> {
            parseHex(hexOrRgba)
          }

          hexOrRgba.startsWith("rgba") -> {
            parseRgba(hexOrRgba)
          }

          hexOrRgba.startsWith("rgb") -> {
            parseRgb(hexOrRgba)
          }

          else -> {
            throw IllegalArgumentException("Parsing not supported for <$hexOrRgba>")
          }
        }
      }
    }

    /**
     * Black with 50% opacity
     */
    @JvmField
    val black50percent: () -> RgbaColor = RgbaColor(1.0, 1.0, 1.0, 0.5).asProvider()

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * These fields have been generated from the javafx Color using com.meistercharts.fx.CanvasPaintProviderGenerator
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     */
    @JvmField
    val transparent: () -> RgbaColor = RgbaColor(0, 0, 0, 0.0).asProvider()

    @JvmField
    val aliceblue: () -> RgbaColor = RgbaColor(240, 248, 255).asProvider()

    @JvmField
    val antiquewhite: () -> RgbaColor = RgbaColor(250, 235, 215).asProvider()

    @JvmField
    val aqua: () -> RgbaColor = RgbaColor(0, 255, 255).asProvider()

    @JvmField
    val aquamarine: () -> RgbaColor = RgbaColor(127, 255, 212).asProvider()

    @JvmField
    val azure: () -> RgbaColor = RgbaColor(240, 255, 255).asProvider()

    @JvmField
    val beige: () -> RgbaColor = RgbaColor(245, 245, 220).asProvider()

    @JvmField
    val bisque: () -> RgbaColor = RgbaColor(255, 228, 196).asProvider()

    @JvmField
    val black: () -> RgbaColor = RgbaColor(0, 0, 0).asProvider()

    @JvmField
    val blanchedalmond: () -> RgbaColor = RgbaColor(255, 235, 205).asProvider()

    @JvmField
    val blue: () -> RgbaColor = RgbaColor(0, 0, 255).asProvider()

    @JvmField
    val blue2: () -> RgbaColor = RgbaColor(0, 125, 192).asProvider()

    @JvmField
    val blue3: () -> RgbaColor = RgbaColor(0, 92, 142).asProvider()

    @JvmField
    val blueviolet: () -> RgbaColor = RgbaColor(138, 43, 226).asProvider()

    @JvmField
    val brown: () -> RgbaColor = RgbaColor(165, 42, 42).asProvider()

    @JvmField
    val burlywood: () -> RgbaColor = RgbaColor(222, 184, 135).asProvider()

    @JvmField
    val cadetblue: () -> RgbaColor = RgbaColor(95, 158, 160).asProvider()

    @JvmField
    val chartreuse: () -> RgbaColor = RgbaColor(127, 255, 0).asProvider()

    @JvmField
    val chocolate: () -> RgbaColor = RgbaColor(210, 105, 30).asProvider()

    @JvmField
    val coral: () -> RgbaColor = RgbaColor(255, 127, 80).asProvider()

    @JvmField
    val cornflowerblue: () -> RgbaColor = RgbaColor(100, 149, 237).asProvider()

    @JvmField
    val cornsilk: () -> RgbaColor = RgbaColor(255, 248, 220).asProvider()

    @JvmField
    val crimson: () -> RgbaColor = RgbaColor(220, 20, 60).asProvider()

    @JvmField
    val cyan: () -> RgbaColor = RgbaColor(0, 255, 255).asProvider()

    @JvmField
    val darkblue: () -> RgbaColor = RgbaColor(0, 0, 139).asProvider()

    @JvmField
    val darkcyan: () -> RgbaColor = RgbaColor(0, 139, 139).asProvider()

    @JvmField
    val darkgoldenrod: () -> RgbaColor = RgbaColor(184, 134, 11).asProvider()

    @JvmField
    val darkgray: () -> RgbaColor = RgbaColor(169, 169, 169).asProvider()

    @JvmField
    val darkergray: () -> RgbaColor = RgbaColor(101, 101, 101).asProvider()

    @JvmField
    val darkgreen: () -> RgbaColor = RgbaColor(0, 100, 0).asProvider()

    @JvmField
    val darkgrey: () -> RgbaColor = RgbaColor(169, 169, 169).asProvider()

    @JvmField
    val darkkhaki: () -> RgbaColor = RgbaColor(189, 183, 107).asProvider()

    @JvmField
    val darkmagenta: () -> RgbaColor = RgbaColor(139, 0, 139).asProvider()

    @JvmField
    val darkolivegreen: () -> RgbaColor = RgbaColor(85, 107, 47).asProvider()

    @JvmField
    val darkorange: () -> RgbaColor = RgbaColor(255, 140, 0).asProvider()

    @JvmField
    val darkorchid: () -> RgbaColor = RgbaColor(153, 50, 204).asProvider()

    @JvmField
    val darkred: () -> RgbaColor = RgbaColor(139, 0, 0).asProvider()

    @JvmField
    val darksalmon: () -> RgbaColor = RgbaColor(233, 150, 122).asProvider()

    @JvmField
    val darkseagreen: () -> RgbaColor = RgbaColor(143, 188, 143).asProvider()

    @JvmField
    val darkslateblue: () -> RgbaColor = RgbaColor(72, 61, 139).asProvider()

    @JvmField
    val darkslategray: () -> RgbaColor = RgbaColor(47, 79, 79).asProvider()

    @JvmField
    val darkslategrey: () -> RgbaColor = RgbaColor(47, 79, 79).asProvider()

    @JvmField
    val darkturquoise: () -> RgbaColor = RgbaColor(0, 206, 209).asProvider()

    @JvmField
    val darkviolet: () -> RgbaColor = RgbaColor(148, 0, 211).asProvider()

    @JvmField
    val deeppink: () -> RgbaColor = RgbaColor(255, 20, 147).asProvider()

    @JvmField
    val deepskyblue: () -> RgbaColor = RgbaColor(0, 191, 255).asProvider()

    @JvmField
    val dimgray: () -> RgbaColor = RgbaColor(105, 105, 105).asProvider()

    @JvmField
    val dimgrey: () -> RgbaColor = RgbaColor(105, 105, 105).asProvider()

    @JvmField
    val dodgerblue: () -> RgbaColor = RgbaColor(30, 144, 255).asProvider()

    @JvmField
    val firebrick: () -> RgbaColor = RgbaColor(178, 34, 34).asProvider()

    @JvmField
    val floralwhite: () -> RgbaColor = RgbaColor(255, 250, 240).asProvider()

    @JvmField
    val forestgreen: () -> RgbaColor = RgbaColor(34, 139, 34).asProvider()

    @JvmField
    val fuchsia: () -> RgbaColor = RgbaColor(255, 0, 255).asProvider()

    @JvmField
    val gainsboro: () -> RgbaColor = RgbaColor(220, 220, 220).asProvider()

    @JvmField
    val ghostwhite: () -> RgbaColor = RgbaColor(248, 248, 255).asProvider()

    @JvmField
    val gold: () -> RgbaColor = RgbaColor(255, 215, 0).asProvider()

    @JvmField
    val goldenrod: () -> RgbaColor = RgbaColor(218, 165, 32).asProvider()

    @JvmField
    val gray: () -> RgbaColor = RgbaColor(128, 128, 128).asProvider()

    @JvmField
    val green: () -> RgbaColor = RgbaColor(0, 128, 0).asProvider()

    @JvmField
    val greenyellow: () -> RgbaColor = RgbaColor(173, 255, 47).asProvider()

    @JvmField
    val grey: () -> RgbaColor = RgbaColor(128, 128, 128).asProvider()

    @JvmField
    val honeydew: () -> RgbaColor = RgbaColor(240, 255, 240).asProvider()

    @JvmField
    val hotpink: () -> RgbaColor = RgbaColor(255, 105, 180).asProvider()

    @JvmField
    val indianred: () -> RgbaColor = RgbaColor(205, 92, 92).asProvider()

    @JvmField
    val indigo: () -> RgbaColor = RgbaColor(75, 0, 130).asProvider()

    @JvmField
    val ivory: () -> RgbaColor = RgbaColor(255, 255, 240).asProvider()

    @JvmField
    val khaki: () -> RgbaColor = RgbaColor(240, 230, 140).asProvider()

    @JvmField
    val lavender: () -> RgbaColor = RgbaColor(230, 230, 250).asProvider()

    @JvmField
    val lavenderblush: () -> RgbaColor = RgbaColor(255, 240, 245).asProvider()

    @JvmField
    val lawngreen: () -> RgbaColor = RgbaColor(124, 252, 0).asProvider()

    @JvmField
    val lemonchiffon: () -> RgbaColor = RgbaColor(255, 250, 205).asProvider()

    @JvmField
    val lightblue: () -> RgbaColor = RgbaColor(173, 216, 230).asProvider()

    @JvmField
    val lightcoral: () -> RgbaColor = RgbaColor(240, 128, 128).asProvider()

    @JvmField
    val lightcyan: () -> RgbaColor = RgbaColor(224, 255, 255).asProvider()

    @JvmField
    val lightgoldenrodyellow: () -> RgbaColor = RgbaColor(250, 250, 210).asProvider()

    @JvmField
    val lightgray: () -> RgbaColor = RgbaColor(211, 211, 211).asProvider()

    @JvmField
    val lightgreen: () -> RgbaColor = RgbaColor(144, 238, 144).asProvider()

    @JvmField
    val lightgrey: () -> RgbaColor = RgbaColor(211, 211, 211).asProvider()

    @JvmField
    val lightpink: () -> RgbaColor = RgbaColor(255, 182, 193).asProvider()

    @JvmField
    val lightsalmon: () -> RgbaColor = RgbaColor(255, 160, 122).asProvider()

    @JvmField
    val lightseagreen: () -> RgbaColor = RgbaColor(32, 178, 170).asProvider()

    @JvmField
    val lightskyblue: () -> RgbaColor = RgbaColor(135, 206, 250).asProvider()

    @JvmField
    val lightslategray: () -> RgbaColor = RgbaColor(119, 136, 153).asProvider()

    @JvmField
    val lightslategrey: () -> RgbaColor = RgbaColor(119, 136, 153).asProvider()

    @JvmField
    val lightsteelblue: () -> RgbaColor = RgbaColor(176, 196, 222).asProvider()

    @JvmField
    val lightyellow: () -> RgbaColor = RgbaColor(255, 255, 224).asProvider()

    @JvmField
    val lime: () -> RgbaColor = RgbaColor(0, 255, 0).asProvider()

    @JvmField
    val limegreen: () -> RgbaColor = RgbaColor(50, 205, 50).asProvider()

    @JvmField
    val linen: () -> RgbaColor = RgbaColor(250, 240, 230).asProvider()

    @JvmField
    val magenta: () -> RgbaColor = RgbaColor(255, 0, 255).asProvider()

    @JvmField
    val maroon: () -> RgbaColor = RgbaColor(128, 0, 0).asProvider()

    @JvmField
    val mediumaquamarine: () -> RgbaColor = RgbaColor(102, 205, 170).asProvider()

    @JvmField
    val mediumblue: () -> RgbaColor = RgbaColor(0, 0, 205).asProvider()

    @JvmField
    val mediumorchid: () -> RgbaColor = RgbaColor(186, 85, 211).asProvider()

    @JvmField
    val mediumpurple: () -> RgbaColor = RgbaColor(147, 112, 219).asProvider()

    @JvmField
    val mediumseagreen: () -> RgbaColor = RgbaColor(60, 179, 113).asProvider()

    @JvmField
    val mediumslateblue: () -> RgbaColor = RgbaColor(123, 104, 238).asProvider()

    @JvmField
    val mediumspringgreen: () -> RgbaColor = RgbaColor(0, 250, 154).asProvider()

    @JvmField
    val mediumturquoise: () -> RgbaColor = RgbaColor(72, 209, 204).asProvider()

    @JvmField
    val mediumvioletred: () -> RgbaColor = RgbaColor(199, 21, 133).asProvider()

    @JvmField
    val midnightblue: () -> RgbaColor = RgbaColor(25, 25, 112).asProvider()

    @JvmField
    val mintcream: () -> RgbaColor = RgbaColor(245, 255, 250).asProvider()

    @JvmField
    val mistyrose: () -> RgbaColor = RgbaColor(255, 228, 225).asProvider()

    @JvmField
    val moccasin: () -> RgbaColor = RgbaColor(255, 228, 181).asProvider()

    @JvmField
    val navajowhite: () -> RgbaColor = RgbaColor(255, 222, 173).asProvider()

    @JvmField
    val navy: () -> RgbaColor = RgbaColor(0, 0, 128).asProvider()

    @JvmField
    val oldlace: () -> RgbaColor = RgbaColor(253, 245, 230).asProvider()

    @JvmField
    val olive: () -> RgbaColor = RgbaColor(128, 128, 0).asProvider()

    @JvmField
    val olivedrab: () -> RgbaColor = RgbaColor(107, 142, 35).asProvider()

    @JvmField
    val orange: () -> RgbaColor = RgbaColor(255, 165, 0).asProvider()

    @JvmField
    val orangered: () -> RgbaColor = RgbaColor(255, 69, 0).asProvider()

    @JvmField
    val orchid: () -> RgbaColor = RgbaColor(218, 112, 214).asProvider()

    @JvmField
    val palegoldenrod: () -> RgbaColor = RgbaColor(238, 232, 170).asProvider()

    @JvmField
    val palegreen: () -> RgbaColor = RgbaColor(152, 251, 152).asProvider()

    @JvmField
    val paleturquoise: () -> RgbaColor = RgbaColor(175, 238, 238).asProvider()

    @JvmField
    val palevioletred: () -> RgbaColor = RgbaColor(219, 112, 147).asProvider()

    @JvmField
    val papayawhip: () -> RgbaColor = RgbaColor(255, 239, 213).asProvider()

    @JvmField
    val peachpuff: () -> RgbaColor = RgbaColor(255, 218, 185).asProvider()

    @JvmField
    val peru: () -> RgbaColor = RgbaColor(205, 133, 63).asProvider()

    @JvmField
    val pink: () -> RgbaColor = RgbaColor(255, 192, 203).asProvider()

    @JvmField
    val plum: () -> RgbaColor = RgbaColor(221, 160, 221).asProvider()

    @JvmField
    val powderblue: () -> RgbaColor = RgbaColor(176, 224, 230).asProvider()

    @JvmField
    val purple: () -> RgbaColor = RgbaColor(128, 0, 128).asProvider()

    @JvmField
    val red: () -> RgbaColor = RgbaColor(255, 0, 0).asProvider()

    @JvmField
    val rosybrown: () -> RgbaColor = RgbaColor(188, 143, 143).asProvider()

    @JvmField
    val royalblue: () -> RgbaColor = RgbaColor(65, 105, 225).asProvider()

    @JvmField
    val saddlebrown: () -> RgbaColor = RgbaColor(139, 69, 19).asProvider()

    @JvmField
    val salmon: () -> RgbaColor = RgbaColor(250, 128, 114).asProvider()

    @JvmField
    val sandybrown: () -> RgbaColor = RgbaColor(244, 164, 96).asProvider()

    @JvmField
    val seagreen: () -> RgbaColor = RgbaColor(46, 139, 87).asProvider()

    @JvmField
    val seashell: () -> RgbaColor = RgbaColor(255, 245, 238).asProvider()

    @JvmField
    val sienna: () -> RgbaColor = RgbaColor(160, 82, 45).asProvider()

    @JvmField
    val silver: () -> RgbaColor = RgbaColor(192, 192, 192).asProvider()

    @JvmField
    val skyblue: () -> RgbaColor = RgbaColor(135, 206, 235).asProvider()

    @JvmField
    val slateblue: () -> RgbaColor = RgbaColor(106, 90, 205).asProvider()

    @JvmField
    val slategray: () -> RgbaColor = RgbaColor(112, 128, 144).asProvider()

    @JvmField
    val slategrey: () -> RgbaColor = RgbaColor(112, 128, 144).asProvider()

    @JvmField
    val snow: () -> RgbaColor = RgbaColor(255, 250, 250).asProvider()

    @JvmField
    val springgreen: () -> RgbaColor = RgbaColor(0, 255, 127).asProvider()

    @JvmField
    val steelblue: () -> RgbaColor = RgbaColor(70, 130, 180).asProvider()

    @JvmField
    val tan: () -> RgbaColor = RgbaColor(210, 180, 140).asProvider()

    @JvmField
    val teal: () -> RgbaColor = RgbaColor(0, 128, 128).asProvider()

    @JvmField
    val thistle: () -> RgbaColor = RgbaColor(216, 191, 216).asProvider()

    @JvmField
    val tomato: () -> RgbaColor = RgbaColor(255, 99, 71).asProvider()

    @JvmField
    val turquoise: () -> RgbaColor = RgbaColor(64, 224, 208).asProvider()

    @JvmField
    val violet: () -> RgbaColor = RgbaColor(238, 130, 238).asProvider()

    @JvmField
    val wheat: () -> RgbaColor = RgbaColor(245, 222, 179).asProvider()

    @JvmField
    val white: () -> RgbaColor = RgbaColor(255, 255, 255).asProvider()

    @JvmField
    val whitesmoke: () -> RgbaColor = RgbaColor(245, 245, 245).asProvider()

    @JvmField
    val yellow: () -> RgbaColor = RgbaColor(255, 255, 0).asProvider()

    @JvmField
    val yellowgreen: () -> RgbaColor = RgbaColor(154, 205, 50).asProvider()

    /**
     * END of auto generated code
     */
  }
}
