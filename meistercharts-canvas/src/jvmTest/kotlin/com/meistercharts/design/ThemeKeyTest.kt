package com.meistercharts.design

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 *
 */
class ThemeKeyTest {
  @Suppress("DEPRECATION")
  @Test
  fun testBasics() {
    val key = ThemeKey("da.id") { 42 }
    assertThat(key.resolve()).isEqualTo(42)

    MyTheme.resolve(key).let {
      assertThat(it).isEqualTo(42)
    }

    val value = key.resolve(MyTheme)
    //val valueProvider: IntProvider = key.invoke(MyTheme)
  }

  object MyTheme : DefaultTheme() {
  }
}
