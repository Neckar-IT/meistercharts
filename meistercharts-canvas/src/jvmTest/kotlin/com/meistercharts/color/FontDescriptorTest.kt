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
import com.meistercharts.font.FontDescriptor
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontSize
import com.meistercharts.font.combineWith
import org.junit.jupiter.api.Test

/**
 */
class FontDescriptorTest {
  @Test
  internal fun testEquals() {
    assertThat(FontDescriptor.Default).isEqualTo(FontDescriptor.Default)
    assertThat(FontDescriptor.L).isEqualTo(FontDescriptor.L)
    assertThat(FontDescriptor.L).isNotEqualTo(FontDescriptor.Default)
    assertThat(FontDescriptor.Default).isNotEqualTo(FontDescriptor.L)
  }

  @Test
  fun testCombine() {
    assertThat(FontDescriptor.Default.combineWith(FontDescriptor.L)).isEqualTo(FontDescriptor.L)
    assertThat(FontDescriptor.L.combineWith(FontDescriptor.L)).isEqualTo(FontDescriptor.L)
    assertThat(FontDescriptor.L.combineWith(FontDescriptor.Default)).isEqualTo(FontDescriptor.Default)
  }

  @Test
  internal fun testFragmentCombine() {
    FontDescriptor.Default.combineWith(FontDescriptorFragment(size = FontSize(17.0))).also {
      assertThat(it.size.size).isEqualTo(17.0)
      assertThat(it.family).isEqualTo(FontDescriptor.Default.family)
    }
  }
}
