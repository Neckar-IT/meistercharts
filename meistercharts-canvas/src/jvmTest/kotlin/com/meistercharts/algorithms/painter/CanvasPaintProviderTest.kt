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

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class CanvasPaintProviderTest {
  @Test
  fun testRgb() {
    assertThat(Color.rgb(0, 0, 0)).isEqualTo(Color.web("#000000"))
    assertThat(Color.rgb(255, 255, 255)).isEqualTo(Color.web("#FFFFFF"))

    assertThat(Color.rgb(7, 8, 255)).isEqualTo(Color.web("#0708FF"))
    assertThat(Color.rgba(7, 8, 255, 0.5)).isEqualTo(Color.web("rgba(7,8,255,0.5)"))

    try {
      Color.rgb(7, 8, 256)
      fail("no exception?")
    } catch (ignore: IllegalArgumentException) {
    }
    try {
      Color.rgb(7, 8, -1)
      fail("no exception?")
    } catch (ignore: IllegalArgumentException) {
    }
    try {
      Color.rgb(7, 256, 8)
      fail("no exception?")
    } catch (ignore: IllegalArgumentException) {
    }
    try {
      Color.rgb(7, -1, 30)
      fail("no exception?")
    } catch (ignore: IllegalArgumentException) {
    }
    try {
      Color.rgb(256, 8, 30)
      fail("no exception?")
    } catch (ignore: IllegalArgumentException) {
    }
    try {
      Color.rgb(-1, 8, 30)
      fail("no exception?")
    } catch (ignore: IllegalArgumentException) {
    }
  }

  @Test
  internal fun testCreation() {
    assertThat(Color.color(1.0, 1.0, 1.0)).isEqualTo(Color.web("#FFFFFF"))
    assertThat(Color.color(0.0, 0.0, 0.0)).isEqualTo(Color.web("#000000"))
    assertThat(Color.color(0.0, 0.0, 0.0, 1.0)).isEqualTo(Color.web("rgba(0,0,0,1)"))
    assertThat(Color.color(0.0, 0.0, 0.0, 0.0)).isEqualTo(Color.web("rgba(0,0,0,0)"))
    assertThat(Color.color(0.5, 0.5, 0.5, 0.5)).isEqualTo(Color.web("rgba(128,128,128,0.5)"))
    assertThat(Color.color(1.0, 0.5, 0.5, 0.5)).isEqualTo(Color.web("rgba(255,128,128,0.5)"))
  }

  @Test
  fun testHex() {
    assertThat(to2DigitHex(0)).isEqualTo("00")
    assertThat(to2DigitHex(255)).isEqualTo("FF")
    assertThat(to2DigitHex(127)).isEqualTo("7F")
    assertThat(to2DigitHex(-255)).isEqualTo("-FF")
  }

}
