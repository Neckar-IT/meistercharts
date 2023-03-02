package com.meistercharts.design

import assertk.*
import assertk.assertions.*
import com.meistercharts.design.CorporateDesign
import com.meistercharts.design.NeckarITDesign
import com.meistercharts.design.Theme
import com.meistercharts.design.ThemeKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.reflect.full.memberProperties

/**
 *
 */
class ThemeTest {
  lateinit var corporateDesign: CorporateDesign

  @BeforeEach
  internal fun setUp() {
    corporateDesign = NeckarITDesign
  }

  @Test
  fun testExample() {
    //Test fallback
    corporateDesign.resolve(ThemeKey("does.not.exist") { NeckarITDesign.textFont }).let {
      assertThat(it).isEqualTo(NeckarITDesign.textFont)
    }
  }

  @Test
  fun testUniqueKeys() {
    val set = mutableSetOf<String>()

    Theme::class.memberProperties.forEach {
      val themeKey = it.get(Theme) as ThemeKey<*>

      if (set.contains(themeKey.id)) {
        fail("Duplicate key <${themeKey.id}>")
      }

      set.add(themeKey.id)
    }
  }
}
