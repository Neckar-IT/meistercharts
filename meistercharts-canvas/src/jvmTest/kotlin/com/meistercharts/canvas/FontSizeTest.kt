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
package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import com.meistercharts.font.FontSize
import org.junit.jupiter.api.Test

class FontSizeTest {
  @Test
  fun testPoints2pixels() {
    assertThat(FontSize.points2pixels(6.0)).isEqualTo(8.0)
    assertThat(FontSize.points2pixels(7.0)).isEqualTo(9.0)
    assertThat(FontSize.points2pixels(7.5)).isEqualTo(10.0)
    assertThat(FontSize.points2pixels(24.0)).isEqualTo(32.0)
  }
}
