package com.meistercharts.font

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 */
class FontDescriptorTest {
  @Test
  fun testGenericFamily() {
    assertThat(FontDescriptor.Default.genericFamily).isEqualTo(GenericFamily.SansSerif)
    assertThat(FontDescriptor.Default.combineWith(FontDescriptorFragment(genericFamily = GenericFamily.Math)).genericFamily).isEqualTo(GenericFamily.Math)
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
  internal fun testFragmentCombine() {
    FontDescriptor.Default.combineWith(FontDescriptorFragment(size = FontSize(17.0))).also {
      assertThat(it.size.size).isEqualTo(17.0)
      assertThat(it.family).isEqualTo(FontDescriptor.Default.family)
    }
  }
}
