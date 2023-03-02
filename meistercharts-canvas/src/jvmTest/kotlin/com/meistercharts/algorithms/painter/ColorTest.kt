package com.meistercharts.algorithms.painter

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test
import javax.annotation.Nonnull

class ColorTest {
  @Test
  fun testBasics() {
    assertThat(Color.red.web).isEqualTo("#FF0000")
  }

  @Test
  fun testBrighter() {
    assertThat(Color.red.lighter(0.2).web).isEqualTo("#FF3333")
    assertThat(Color.white.lighter(0.2).web).isEqualTo("#FFFFFF")
  }

  @Test
  fun testDarker() {
    assertThat(Color.red.darker(0.2).web).isEqualTo("#CC0000")
    assertThat(Color.white.darker(0.2).web).isEqualTo("#CCCCCC")
  }

  @Test
  fun testParseHex() {
    assertThat(Color.red.web).isEqualTo("#FF0000")
    assertThat(RgbaColor.parseHex(Color.red.web)).isEqualTo(Color.red)
    assertThat(RgbaColor.parseHex(Color.bisque.web)).isEqualTo(Color.bisque)
  }

  //@Test
  //fun testConvertToRgba() {
  //  val kClass = Color::class
  //  val companionObject = kClass.companionObject!!
  //  companionObject.members.forEach {
  //
  //    if (it is KProperty) {
  //      val result = it.getter.call(companionObject)
  //
  //      if (result is WebColor) {
  //        val webString = result.web
  //
  //        val parsed = RgbaColor.parseHex(webString)
  //
  //        val alpha = parsed.alpha
  //        val alphaSuffix = if (alpha != null) {
  //          ", $alpha"
  //        } else {
  //          ""
  //        }
  //
  //
  //        println(
  //          """
  //          @JvmField
  //          val ${it.name}:RgbaColor  = RgbaColor(${parsed.red}, ${parsed.green}, ${parsed.blue}$alphaSuffix)
  //          """.trimIndent()
  //        )
  //      }
  //    }
  //  }
  //}
}

//@Nonnull
//fun withAlpha(@Nonnull color: java.awt.Color, @pct alpha: Double): java.awt.Color? {
//  return java.awt.Color(color.red, color.green, color.blue, (alpha * 255).toInt())
//}
//
//@Nonnull
//fun brighterWithAlpha(@Nonnull color: java.awt.Color, @pct brightness: Double, @pct alpha: Double): java.awt.Color? {
//  return java.awt.Color(brighter(color.red, brightness), brighter(color.green, brightness), brighter(color.blue, brightness), (alpha * 255).toInt())
//}

/**
 * Compares two colors that may be nullable
 */
//fun compare(color1: java.awt.Color?, color2: java.awt.Color?): Int {
//  val colorAsFloat1: Float = color1?.let { color2float(it) } ?: -1
//  val colorAsFloat2: Float = color2?.let { color2float(it) } ?: -1
//  return java.lang.Float.compare(colorAsFloat1, colorAsFloat2)
//}

/**
 * get the color that matches the percent value between start hue and hue
 */
@Nonnull
fun interpolate(percent: Double, startHue: Double, endHue: Double): java.awt.Color? {
  val saturation = 0.35f
  val brightness = 1.0f
  return interpolate(percent, startHue, endHue, saturation, brightness)
}

@Nonnull
fun interpolate(percent: Double, startHue: Double, endHue: Double, saturation: Float, brightness: Float): java.awt.Color? {
  val range: Double
  range = if (endHue > startHue) {
    endHue - startHue
  } else {
    1 - (startHue - endHue)
  }
  val hue = percent * range + startHue
  return java.awt.Color.getHSBColor(hue.toFloat(), saturation, brightness)
}
