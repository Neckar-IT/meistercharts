package com.meistercharts.font

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 *
 */
class FontFamilyTest {
  @Test
  fun testFontFamily() {
    val families = listOf(FontFamily("Arial"), FontFamily("Verdana"), FontFamily("Tahoma"))
    assertThat(families).hasSize(3)
    assertThat(families.none { family -> family.isProbablyDefaultSerifFamily() }).isTrue()
  }
}
