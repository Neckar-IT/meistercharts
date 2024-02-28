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

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test
import javax.annotation.Nonnull

class ColorTest {
  @Test
  fun testParseHexOrRgb() {
    assertThat(Color.parseHexOrRgba("#FF0000")).isEqualTo(Color.red())
    assertThat(Color.parseHexOrRgba("rgba(255, 0, 0, 1)")).isEqualTo(Color.red())
    assertThat(Color.parseHexOrRgba("rgba(255, 0, 0, 0.5)")).isEqualTo(Color.red().withAlpha(0.5))
  }

  @Test
  fun testBasics() {
    assertThat(Color.red().web).isEqualTo("#FF0000")
  }

  @Test
  fun testBrighter() {
    assertThat(Color.red().lighter(0.2).web).isEqualTo("#FF3333")
    assertThat(Color.white().lighter(0.2).web).isEqualTo("#FFFFFF")
  }

  @Test
  fun testDarker() {
    assertThat(Color.red().darker(0.2).web).isEqualTo("#CC0000")
    assertThat(Color.white().darker(0.2).web).isEqualTo("#CCCCCC")
  }

  @Test
  fun testParseHex() {
    assertThat(Color.red().web).isEqualTo("#FF0000")
    assertThat(Color.parseHex(Color.red().web)).isEqualTo(Color.red())
    assertThat(Color.parseHex(Color.bisque().web)).isEqualTo(Color.bisque())
  }

  @Test
  fun testParseHex8Digits() {
    assertThat(Color.parseHex("#FF0000FF")).isEqualTo(Color.red())
  }
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
