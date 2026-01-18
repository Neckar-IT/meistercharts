/*
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
package com.meistercharts.font

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 */
class FontDescriptorTest {
  @Test
  fun testGenericFamily() {
    assertThat(FontDescriptor.Default.genericFamily).isEqualTo(GenericFontFamily.SansSerif)
    assertThat(FontDescriptor.Default.combineWith(FontDescriptorFragment(genericFamily = GenericFontFamily.Math)).genericFamily).isEqualTo(GenericFontFamily.Math)
  }

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
  fun testFontDescriptorFamilyHtmlString() {
    assertThat(
      FontDescriptor(
        families = listOf(FontFamily.Oswald),
      ).toHtmlFontString()
    ).isEqualTo(""""Oswald",sans-serif""")

    assertThat(
      FontDescriptor(
        families = listOf(),
      ).toHtmlFontString()
    ).isEqualTo("""sans-serif""")

    assertThat(
      FontDescriptor(
        families = null,
      ).toHtmlFontString()
    ).isEqualTo("""sans-serif""")

    assertThat(
      FontDescriptor(
        families = listOf(FontFamily.Oswald, FontFamily.Arial, FontFamily.TimesNewRoman),
      ).toHtmlFontString()
    ).isEqualTo(""""Oswald","Arial","Times New Roman",sans-serif""")
  }

  @Test
  fun testMultipleFontDescriptorFamiliesHtmlString() {
    val descriptor = FontDescriptor(
      families = listOf(FontFamily.Oswald, FontFamily.TimesNewRoman, FontFamily.OpenSans),
      genericFamily = GenericFontFamily.SystemUi
    )
    assertThat(descriptor.toHtmlFontString()).isEqualTo(""""Oswald","Times New Roman","Open Sans",system-ui""")
  }

  @Test
  fun testFontDescriptorEmptyFamiliesHtmlString() {
    val descriptor = FontDescriptor(
      families = listOf(),
      genericFamily = GenericFontFamily.SystemUi
    )
    assertThat(descriptor.toHtmlFontString()).isEqualTo("""system-ui""")
  }

  @Test
  fun testFontDescriptorNullFamiliesHtmlString() {
    val descriptor = FontDescriptor(
      families = null,
      genericFamily = GenericFontFamily.SystemUi
    )
    assertThat(descriptor.toHtmlFontString()).isEqualTo("""system-ui""")
  }

  @Test
  fun testFontDescriptorEraseSoleSerif() {
    val descriptorFragment = FontDescriptor(
      families = listOf(FontFamily.TimesNewRoman),
      genericFamily = GenericFontFamily.SystemUi
    ).eraseSoleSerifFamily()

    val descriptorFragmentDiffSerif = FontDescriptor(
      families = listOf(FontFamily("serif")),
      genericFamily = GenericFontFamily.SystemUi
    ).eraseSoleSerifFamily()

    assertThat(descriptorFragment.families).isNull()
    assertThat(descriptorFragmentDiffSerif.families).isNull()
  }


  @Test
  internal fun testFragmentCombine() {
    FontDescriptor.Default.combineWith(FontDescriptorFragment(size = FontSize(17.0))).also {
      assertThat(it.size.size).isEqualTo(17.0)
      assertThat(it.families).isEqualTo(FontDescriptor.Default.families)
    }
  }
}
