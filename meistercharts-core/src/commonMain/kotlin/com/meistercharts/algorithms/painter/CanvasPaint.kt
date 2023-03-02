package com.meistercharts.algorithms.painter

import it.neckar.open.kotlin.lang.random
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.roundToInt


/**
 * A paint that can be set on a canvas. Supports flat colors and gradients.
 */
//Should be a sealed class. But since Color must be an interface (Kotlin bug related to initializers and companion objects), this is also an interface for now
sealed interface CanvasPaint

/**
 * A flat color with RGB + optional alpha
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
  val alpha: @pct Double? = null
) : Color {
  init {
    require((0..255).contains(red)) { "Red out of range: <${red}>. Allowed values between 0 and 255" }
    require((0..255).contains(green)) { "Green out of range: <${green}>. Allowed values between 0 and 255" }
    require((0..255).contains(blue)) { "Blue out of range: <${blue}>. Allowed values between 0 and 255" }

    if (alpha != null) {
      require((0.0..1.0).contains(alpha)) { "Alpha out of range: <$alpha>. Allowed values between 0.0 and 1.0" }
    }
  }

  override val web: String = toWebString(red, green, blue, alpha)

  /**
   * Returns an rgb string
   */
  fun formatRgba(): String {
    return toRgbaString(red, green, blue, alpha)
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
    return RgbaColor(red, green, blue, newAlpha)
  }

  companion object {
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
      alpha: @pct Double? = null
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

    /**
     * Parses a hex string (#23112211)
     */
    fun parseHex(web: String): RgbaColor {
      require(web.startsWith("#")) { "Hex string must start with # but was <$web>" }

      val hex = web.removePrefix("#")

      require(hex.length == 6) { "Only hex strings in the format #112233 are supported but was <$web>" }

      val red = hex.substring(0, 2).parse2DigitHex()
      val green = hex.substring(2, 4).parse2DigitHex()
      val blue = hex.substring(4, 6).parse2DigitHex()

      return RgbaColor(red, green, blue)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Color) return false

    if (web != other.web) return false

    return true
  }

  override fun hashCode(): Int {
    return web.hashCode()
  }

  override fun toString(): String {
    return web
  }
}

/**
 * Represents a flat color.
 *
 * ATTENTION: [equals] and [hashCode] use *only* the [web] representation of the color.
 * There are no other checks.
 *
 * Therefore different sub classes that result in the same web string, are equal.
 * But different web strings that will represent the same color, are *not* equal.
 */
sealed interface Color : CanvasPaint, CanvasPaintProvider {
  /**
   * Returns a web string that represents this color
   */
  val web: String

  companion object {
    /**
     * Creates a web string
     */
    operator fun invoke(web: String): WebColor {
      return web(web)
    }

    /**
     * Creates a color from a web string.
     *
     * Beware that the #RRGGBBAA notation is not supported by IE11
     */
    @JvmStatic
    fun web(web: String): WebColor {
      return WebColor(web)
    }

    /**
     * Creates a color with RGB values (0..1)
     */
    @JvmStatic
    fun color(red: Double, green: Double, blue: Double): RgbaColor {
      return color(red, green, blue, null)
    }

    /**
     * Creates a color with RGB values (0..1) and opacity (0..1)
     * @param opacity 0..1
     */
    @JvmStatic
    fun color(red: Double, green: Double, blue: Double, opacity: Double?): RgbaColor {
      return RgbaColor(red, green, blue, opacity)
    }

    /**
     * Creates a color with RGB values (0..255) and opacity (0..1)
     */
    @JvmStatic
    fun rgb(red: Int, green: Int, blue: Int): RgbaColor {
      return rgba(red, green, blue, null)
    }

    /**
     * Creates a color with RGB values (0..255) and opacity (0..1)
     * @param opacity 0..1
     */
    @JvmStatic
    fun rgba(red: Int, green: Int, blue: Int, opacity: Double?): RgbaColor {
      return RgbaColor(red, green, blue, opacity)
    }

    /**
     * Creates a fully opaque, random color
     */
    @JvmStatic
    fun random(): RgbaColor {
      val red = random.nextInt(256)
      val green = random.nextInt(256)
      val blue = random.nextInt(256)
      return rgb(red, green, blue)
    }

    /**
     * Black with 50% opacity
     */
    @JvmField
    val black50percent: Color = RgbaColor(1.0, 1.0, 1.0, 0.5)

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * These fields have been generated from the javafx Color using com.meistercharts.fx.CanvasPaintProviderGenerator
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     */
    @JvmField
    val transparent: RgbaColor = RgbaColor(0, 0, 0, 0.0)

    @JvmField
    val aliceblue: RgbaColor = RgbaColor(240, 248, 255)

    @JvmField
    val antiquewhite: RgbaColor = RgbaColor(250, 235, 215)

    @JvmField
    val aqua: RgbaColor = RgbaColor(0, 255, 255)

    @JvmField
    val aquamarine: RgbaColor = RgbaColor(127, 255, 212)

    @JvmField
    val azure: RgbaColor = RgbaColor(240, 255, 255)

    @JvmField
    val beige: RgbaColor = RgbaColor(245, 245, 220)

    @JvmField
    val bisque: RgbaColor = RgbaColor(255, 228, 196)

    @JvmField
    val black: RgbaColor = RgbaColor(0, 0, 0)

    @JvmField
    val blanchedalmond: RgbaColor = RgbaColor(255, 235, 205)

    @JvmField
    val blue: RgbaColor = RgbaColor(0, 0, 255)

    @JvmField
    val blue2: RgbaColor = RgbaColor(0, 125, 192)

    @JvmField
    val blueviolet: RgbaColor = RgbaColor(138, 43, 226)

    @JvmField
    val brown: RgbaColor = RgbaColor(165, 42, 42)

    @JvmField
    val burlywood: RgbaColor = RgbaColor(222, 184, 135)

    @JvmField
    val cadetblue: RgbaColor = RgbaColor(95, 158, 160)

    @JvmField
    val chartreuse: RgbaColor = RgbaColor(127, 255, 0)

    @JvmField
    val chocolate: RgbaColor = RgbaColor(210, 105, 30)

    @JvmField
    val coral: RgbaColor = RgbaColor(255, 127, 80)

    @JvmField
    val cornflowerblue: RgbaColor = RgbaColor(100, 149, 237)

    @JvmField
    val cornsilk: RgbaColor = RgbaColor(255, 248, 220)

    @JvmField
    val crimson: RgbaColor = RgbaColor(220, 20, 60)

    @JvmField
    val cyan: RgbaColor = RgbaColor(0, 255, 255)

    @JvmField
    val darkblue: RgbaColor = RgbaColor(0, 0, 139)

    @JvmField
    val darkcyan: RgbaColor = RgbaColor(0, 139, 139)

    @JvmField
    val darkgoldenrod: RgbaColor = RgbaColor(184, 134, 11)

    @JvmField
    val darkgray: RgbaColor = RgbaColor(169, 169, 169)

    @JvmField
    val darkgreen: RgbaColor = RgbaColor(0, 100, 0)

    @JvmField
    val darkgrey: RgbaColor = RgbaColor(169, 169, 169)

    @JvmField
    val darkkhaki: RgbaColor = RgbaColor(189, 183, 107)

    @JvmField
    val darkmagenta: RgbaColor = RgbaColor(139, 0, 139)

    @JvmField
    val darkolivegreen: RgbaColor = RgbaColor(85, 107, 47)

    @JvmField
    val darkorange: RgbaColor = RgbaColor(255, 140, 0)

    @JvmField
    val darkorchid: RgbaColor = RgbaColor(153, 50, 204)

    @JvmField
    val darkred: RgbaColor = RgbaColor(139, 0, 0)

    @JvmField
    val darksalmon: RgbaColor = RgbaColor(233, 150, 122)

    @JvmField
    val darkseagreen: RgbaColor = RgbaColor(143, 188, 143)

    @JvmField
    val darkslateblue: RgbaColor = RgbaColor(72, 61, 139)

    @JvmField
    val darkslategray: RgbaColor = RgbaColor(47, 79, 79)

    @JvmField
    val darkslategrey: RgbaColor = RgbaColor(47, 79, 79)

    @JvmField
    val darkturquoise: RgbaColor = RgbaColor(0, 206, 209)

    @JvmField
    val darkviolet: RgbaColor = RgbaColor(148, 0, 211)

    @JvmField
    val deeppink: RgbaColor = RgbaColor(255, 20, 147)

    @JvmField
    val deepskyblue: RgbaColor = RgbaColor(0, 191, 255)

    @JvmField
    val dimgray: RgbaColor = RgbaColor(105, 105, 105)

    @JvmField
    val dimgrey: RgbaColor = RgbaColor(105, 105, 105)

    @JvmField
    val dodgerblue: RgbaColor = RgbaColor(30, 144, 255)

    @JvmField
    val firebrick: RgbaColor = RgbaColor(178, 34, 34)

    @JvmField
    val floralwhite: RgbaColor = RgbaColor(255, 250, 240)

    @JvmField
    val forestgreen: RgbaColor = RgbaColor(34, 139, 34)

    @JvmField
    val fuchsia: RgbaColor = RgbaColor(255, 0, 255)

    @JvmField
    val gainsboro: RgbaColor = RgbaColor(220, 220, 220)

    @JvmField
    val ghostwhite: RgbaColor = RgbaColor(248, 248, 255)

    @JvmField
    val gold: RgbaColor = RgbaColor(255, 215, 0)

    @JvmField
    val goldenrod: RgbaColor = RgbaColor(218, 165, 32)

    @JvmField
    val gray: RgbaColor = RgbaColor(128, 128, 128)

    @JvmField
    val green: RgbaColor = RgbaColor(0, 128, 0)

    @JvmField
    val greenyellow: RgbaColor = RgbaColor(173, 255, 47)

    @JvmField
    val grey: RgbaColor = RgbaColor(128, 128, 128)

    @JvmField
    val honeydew: RgbaColor = RgbaColor(240, 255, 240)

    @JvmField
    val hotpink: RgbaColor = RgbaColor(255, 105, 180)

    @JvmField
    val indianred: RgbaColor = RgbaColor(205, 92, 92)

    @JvmField
    val indigo: RgbaColor = RgbaColor(75, 0, 130)

    @JvmField
    val ivory: RgbaColor = RgbaColor(255, 255, 240)

    @JvmField
    val khaki: RgbaColor = RgbaColor(240, 230, 140)

    @JvmField
    val lavender: RgbaColor = RgbaColor(230, 230, 250)

    @JvmField
    val lavenderblush: RgbaColor = RgbaColor(255, 240, 245)

    @JvmField
    val lawngreen: RgbaColor = RgbaColor(124, 252, 0)

    @JvmField
    val lemonchiffon: RgbaColor = RgbaColor(255, 250, 205)

    @JvmField
    val lightblue: RgbaColor = RgbaColor(173, 216, 230)

    @JvmField
    val lightcoral: RgbaColor = RgbaColor(240, 128, 128)

    @JvmField
    val lightcyan: RgbaColor = RgbaColor(224, 255, 255)

    @JvmField
    val lightgoldenrodyellow: RgbaColor = RgbaColor(250, 250, 210)

    @JvmField
    val lightgray: RgbaColor = RgbaColor(211, 211, 211)

    @JvmField
    val lightgreen: RgbaColor = RgbaColor(144, 238, 144)

    @JvmField
    val lightgrey: RgbaColor = RgbaColor(211, 211, 211)

    @JvmField
    val lightpink: RgbaColor = RgbaColor(255, 182, 193)

    @JvmField
    val lightsalmon: RgbaColor = RgbaColor(255, 160, 122)

    @JvmField
    val lightseagreen: RgbaColor = RgbaColor(32, 178, 170)

    @JvmField
    val lightskyblue: RgbaColor = RgbaColor(135, 206, 250)

    @JvmField
    val lightslategray: RgbaColor = RgbaColor(119, 136, 153)

    @JvmField
    val lightslategrey: RgbaColor = RgbaColor(119, 136, 153)

    @JvmField
    val lightsteelblue: RgbaColor = RgbaColor(176, 196, 222)

    @JvmField
    val lightyellow: RgbaColor = RgbaColor(255, 255, 224)

    @JvmField
    val lime: RgbaColor = RgbaColor(0, 255, 0)

    @JvmField
    val limegreen: RgbaColor = RgbaColor(50, 205, 50)

    @JvmField
    val linen: RgbaColor = RgbaColor(250, 240, 230)

    @JvmField
    val magenta: RgbaColor = RgbaColor(255, 0, 255)

    @JvmField
    val maroon: RgbaColor = RgbaColor(128, 0, 0)

    @JvmField
    val mediumaquamarine: RgbaColor = RgbaColor(102, 205, 170)

    @JvmField
    val mediumblue: RgbaColor = RgbaColor(0, 0, 205)

    @JvmField
    val mediumorchid: RgbaColor = RgbaColor(186, 85, 211)

    @JvmField
    val mediumpurple: RgbaColor = RgbaColor(147, 112, 219)

    @JvmField
    val mediumseagreen: RgbaColor = RgbaColor(60, 179, 113)

    @JvmField
    val mediumslateblue: RgbaColor = RgbaColor(123, 104, 238)

    @JvmField
    val mediumspringgreen: RgbaColor = RgbaColor(0, 250, 154)

    @JvmField
    val mediumturquoise: RgbaColor = RgbaColor(72, 209, 204)

    @JvmField
    val mediumvioletred: RgbaColor = RgbaColor(199, 21, 133)

    @JvmField
    val midnightblue: RgbaColor = RgbaColor(25, 25, 112)

    @JvmField
    val mintcream: RgbaColor = RgbaColor(245, 255, 250)

    @JvmField
    val mistyrose: RgbaColor = RgbaColor(255, 228, 225)

    @JvmField
    val moccasin: RgbaColor = RgbaColor(255, 228, 181)

    @JvmField
    val navajowhite: RgbaColor = RgbaColor(255, 222, 173)

    @JvmField
    val navy: RgbaColor = RgbaColor(0, 0, 128)

    @JvmField
    val oldlace: RgbaColor = RgbaColor(253, 245, 230)

    @JvmField
    val olive: RgbaColor = RgbaColor(128, 128, 0)

    @JvmField
    val olivedrab: RgbaColor = RgbaColor(107, 142, 35)

    @JvmField
    val orange: RgbaColor = RgbaColor(255, 165, 0)

    @JvmField
    val orangered: RgbaColor = RgbaColor(255, 69, 0)

    @JvmField
    val orchid: RgbaColor = RgbaColor(218, 112, 214)

    @JvmField
    val palegoldenrod: RgbaColor = RgbaColor(238, 232, 170)

    @JvmField
    val palegreen: RgbaColor = RgbaColor(152, 251, 152)

    @JvmField
    val paleturquoise: RgbaColor = RgbaColor(175, 238, 238)

    @JvmField
    val palevioletred: RgbaColor = RgbaColor(219, 112, 147)

    @JvmField
    val papayawhip: RgbaColor = RgbaColor(255, 239, 213)

    @JvmField
    val peachpuff: RgbaColor = RgbaColor(255, 218, 185)

    @JvmField
    val peru: RgbaColor = RgbaColor(205, 133, 63)

    @JvmField
    val pink: RgbaColor = RgbaColor(255, 192, 203)

    @JvmField
    val plum: RgbaColor = RgbaColor(221, 160, 221)

    @JvmField
    val powderblue: RgbaColor = RgbaColor(176, 224, 230)

    @JvmField
    val purple: RgbaColor = RgbaColor(128, 0, 128)

    @JvmField
    val red: RgbaColor = RgbaColor(255, 0, 0)

    @JvmField
    val rosybrown: RgbaColor = RgbaColor(188, 143, 143)

    @JvmField
    val royalblue: RgbaColor = RgbaColor(65, 105, 225)

    @JvmField
    val saddlebrown: RgbaColor = RgbaColor(139, 69, 19)

    @JvmField
    val salmon: RgbaColor = RgbaColor(250, 128, 114)

    @JvmField
    val sandybrown: RgbaColor = RgbaColor(244, 164, 96)

    @JvmField
    val seagreen: RgbaColor = RgbaColor(46, 139, 87)

    @JvmField
    val seashell: RgbaColor = RgbaColor(255, 245, 238)

    @JvmField
    val sienna: RgbaColor = RgbaColor(160, 82, 45)

    @JvmField
    val silver: RgbaColor = RgbaColor(192, 192, 192)

    @JvmField
    val skyblue: RgbaColor = RgbaColor(135, 206, 235)

    @JvmField
    val slateblue: RgbaColor = RgbaColor(106, 90, 205)

    @JvmField
    val slategray: RgbaColor = RgbaColor(112, 128, 144)

    @JvmField
    val slategrey: RgbaColor = RgbaColor(112, 128, 144)

    @JvmField
    val snow: RgbaColor = RgbaColor(255, 250, 250)

    @JvmField
    val springgreen: RgbaColor = RgbaColor(0, 255, 127)

    @JvmField
    val steelblue: RgbaColor = RgbaColor(70, 130, 180)

    @JvmField
    val tan: RgbaColor = RgbaColor(210, 180, 140)

    @JvmField
    val teal: RgbaColor = RgbaColor(0, 128, 128)

    @JvmField
    val thistle: RgbaColor = RgbaColor(216, 191, 216)

    @JvmField
    val tomato: RgbaColor = RgbaColor(255, 99, 71)

    @JvmField
    val turquoise: RgbaColor = RgbaColor(64, 224, 208)

    @JvmField
    val violet: RgbaColor = RgbaColor(238, 130, 238)

    @JvmField
    val wheat: RgbaColor = RgbaColor(245, 222, 179)

    @JvmField
    val white: RgbaColor = RgbaColor(255, 255, 255)

    @JvmField
    val whitesmoke: RgbaColor = RgbaColor(245, 245, 245)

    @JvmField
    val yellow: RgbaColor = RgbaColor(255, 255, 0)

    @JvmField
    val yellowgreen: RgbaColor = RgbaColor(154, 205, 50)

    /**
     * END of auto generated code
     */
  }
}

/**
 * Represents a flat color that is represented through a single web string
 *
 * Alpha values table:
 * * 100% — FF
 * * 99% — FC
 * * 98% — FA
 * * 97% — F7
 * * 96% — F5
 * * 95% — F2
 * * 94% — F0
 * * 93% — ED
 * * 92% — EB
 * * 91% — E8
 * * 90% — E6
 * * 89% — E3
 * * 88% — E0
 * * 87% — DE
 * * 86% — DB
 * * 85% — D9
 * * 84% — D6
 * * 83% — D4
 * * 82% — D1
 * * 81% — CF
 * * 80% — CC
 * * 79% — C9
 * * 78% — C7
 * * 77% — C4
 * * 76% — C2
 * * 75% — BF
 * * 74% — BD
 * * 73% — BA
 * * 72% — B8
 * * 71% — B5
 * * 70% — B3
 * * 69% — B0
 * * 68% — AD
 * * 67% — AB
 * * 66% — A8
 * * 65% — A6
 * * 64% — A3
 * * 63% — A1
 * * 62% — 9E
 * * 61% — 9C
 * * 60% — 99
 * * 59% — 96
 * * 58% — 94
 * * 57% — 91
 * * 56% — 8F
 * * 55% — 8C
 * * 54% — 8A
 * * 53% — 87
 * * 52% — 85
 * * 51% — 82
 * * 50% — 80
 * * 49% — 7D
 * * 48% — 7A
 * * 47% — 78
 * * 46% — 75
 * * 45% — 73
 * * 44% — 70
 * * 43% — 6E
 * * 42% — 6B
 * * 41% — 69
 * * 40% — 66
 * * 39% — 63
 * * 38% — 61
 * * 37% — 5E
 * * 36% — 5C
 * * 35% — 59
 * * 34% — 57
 * * 33% — 54
 * * 32% — 52
 * * 31% — 4F
 * * 30% — 4D
 * * 29% — 4A
 * * 28% — 47
 * * 27% — 45
 * * 26% — 42
 * * 25% — 40
 * * 24% — 3D
 * * 23% — 3B
 * * 22% — 38
 * * 21% — 36
 * * 20% — 33
 * * 19% — 30
 * * 18% — 2E
 * * 17% — 2B
 * * 16% — 29
 * * 15% — 26
 * * 14% — 24
 * * 13% — 21
 * * 12% — 1F
 * * 11% — 1C
 * * 10% — 1A
 * * 9% — 17
 * * 8% — 14
 * * 7% — 12
 * * 6% — 0F
 * * 5% — 0D
 * * 4% — 0A
 * * 3% — 08
 * * 2% — 05
 * * 1% — 03
 * * 0% — 00
 */
data class WebColor(override val web: String) : Color {
  init {
    require(web.isNotEmpty()) { "empty string not allowed" }
    if (web.startsWith("#")) {
      require(!web.contains(' ')) { "Must not contain spaces: <$web>" }
    }
  }

  override fun toCanvasPaint(x0: @px Double, y0: @px Double, x1: @px Double, y1: @px Double): WebColor {
    return this
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Color) return false

    if (web != other.web) return false

    return true
  }

  override fun hashCode(): Int {
    return web.hashCode()
  }

  override fun toString(): String {
    return web
  }
}

/**
 * The simplest linear gradient containing two stops
 */
data class CanvasLinearGradient(
  val x0: @px Double,
  val y0: @px Double,
  val x1: @px Double,
  val y1: @px Double,
  val color0: Color,
  val color1: Color
) : CanvasPaint {

  override fun toString(): String {
    return "LinearGradient($x0/$y0 - $x1/$y1, color0=$color0, color1=$color1)"
  }
}


/**
 * Represents a radial gradient
 */
data class CanvasRadialGradient(
  /**
   * The position of the center
   */
  val positionX: @px Double,
  /**
   * The position of the center
   */
  val positionY: @px Double,

  /**
   * The radius of the gradient
   */
  val radius: @px Double,

  /**
   * The color at the center of the gradient
   */
  val color0: Color,
  /**
   * The outer color
   */
  val color1: Color,

  //TODO add color stops(?)

  //TODO add color hint(?)
) : CanvasPaint {


  @Deprecated("???")
  enum class Shape {
    Circle,
    Ellipse
  }

  /**
   * The extend of the radial gradient
   * [https://developer.mozilla.org/en-US/docs/Web/CSS/radial-gradient]
   */
  @Deprecated("???")
  enum class Extend {
    /**
     * The gradient's ending shape meets the side of the box closest to its center (for circles) or meets both the vertical and horizontal sides closest to the center (for ellipses).
     */
    ClosestSide,

    /**
     * The gradient's ending shape is sized so that it exactly meets the closest corner of the box from its center.
     */
    ClosestCorner,

    /**
     * Similar to closest-side, except the ending shape is sized to meet the side of the box farthest from its center (or vertical and horizontal sides).
     */
    FarthestSide,

    /**
     * The default value, the gradient's ending shape is sized so that it exactly meets the farthest corner of the box from its center.
     */
    FarthestCorner

  }
}
