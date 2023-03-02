package com.meistercharts.design

import assertk.*
import assertk.assertions.*
import com.meistercharts.design.SegoeUiDesign
import it.neckar.open.javafx.test.JavaFxTest
import com.meistercharts.fx.font.toFont
import javafx.scene.text.Font
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@JavaFxTest
class SegoeUiDesignTest {
  @Test
  fun testFonts() {
    val fontDescriptor = SegoeUiDesign.h1.withDefaultValues()
    val font = fontDescriptor.toFont()
    assertThat(font).isNotNull()

    assertThat(font.family).isEqualTo("Segoe UI")
  }

  @Disabled
  @Test
  fun testListFonts() {
    println("Font Families")
    println("------------------------")
    Font.getFamilies().forEach {
      println("\t$it")
    }

    println("Font Names")
    println("------------------------")
    Font.getFontNames().forEach {
      println("\t$it")
    }

  }
}
