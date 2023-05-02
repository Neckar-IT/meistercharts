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
