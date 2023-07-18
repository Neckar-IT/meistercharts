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

/**
 *
 */
class RgbaColorTest {
  @Test
  fun testTint() {
    assertThat(RgbaColor(64, 168, 245).formatRgba()).isEqualTo("rgba(64, 168, 245, 1)")

    assertThat(RgbaColor(64, 168, 245).lighter(0.2).formatRgba()).isEqualTo(RgbaColor(102, 185, 247).formatRgba())
    assertThat(RgbaColor(64, 168, 245).lighter(0.7).formatRgba()).isEqualTo(RgbaColor(198, 229, 252).formatRgba())

    assertThat(RgbaColor(159, 211, 213).lighter(0.2).formatRgba()).isEqualTo(RgbaColor(178, 220, 221).formatRgba())
    assertThat(RgbaColor(159, 211, 213).lighter(0.7).formatRgba()).isEqualTo(RgbaColor(226, 242, 242).formatRgba())
  }

  @Test
  fun testParse() {
    assertThat(Color.parseRgba("rgba(64,168,245,0.5)")).isEqualTo(RgbaColor(64, 168, 245, 0.5))
    assertThat(Color.parseRgba("rgba(64, 168, 245, 0.5)")).isEqualTo(RgbaColor(64, 168, 245, 0.5))
    assertThat(Color.parseRgb("rgb(64,168,245)")).isEqualTo(RgbaColor(64, 168, 245, 1.0))
    assertThat(Color.parseRgb("rgb(64, 168, 245)")).isEqualTo(RgbaColor(64, 168, 245, 1.0))
  }

  @Test
  fun testParseSpaces() {
    Color.parseRgba("rgba(64, 168, 245, 0.5)").let {
      assertThat(it.red).isEqualTo(64)
      assertThat(it.green).isEqualTo(168)
      assertThat(it.blue).isEqualTo(245)
      assertThat(it.alpha).isEqualTo(0.5)
    }

    Color.parseRgb("rgb(64, 168, 245)").let {
      assertThat(it.red).isEqualTo(64)
      assertThat(it.green).isEqualTo(168)
      assertThat(it.blue).isEqualTo(245)
      assertThat(it.alpha).isEqualTo(1.0)
    }
  }
}
