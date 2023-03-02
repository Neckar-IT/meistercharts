package com.meistercharts.design

import assertk.*
import assertk.assertions.*
import com.meistercharts.design.NeckarITDesign
import com.meistercharts.fx.toJavaFx
import org.junit.jupiter.api.Test

/**
 *
 */
class NeckarITDesignTest {
  @Test
  fun testColors() {
    NeckarITDesign.colors().forEach {
      assertThat(it, it.toString()).isNotNull()
      try {
        val javaFx = it.toJavaFx()
        assertThat(javaFx).isNotNull()
      } catch (e: Exception) {
        fail("Could not convert $it")
      }
    }
  }
}
