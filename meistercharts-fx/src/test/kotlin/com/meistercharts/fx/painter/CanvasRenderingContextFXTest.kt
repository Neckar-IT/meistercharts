package com.meistercharts.fx.painter

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasType
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontFamily
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.saved
import com.meistercharts.design.corporateDesign
import it.neckar.open.javafx.test.JavaFxTest
import com.meistercharts.fx.CanvasFX
import com.meistercharts.fx.CanvasRenderingContextFX
import org.junit.jupiter.api.Test

@JavaFxTest
internal class CanvasRenderingContextFXTest {
  @Test
  internal fun testColorInstantiations() {
    val canvas = CanvasFX(type = CanvasType.OffScreen)
    val gc = canvas.canvas.graphicsContext2D
    val graphicsContext = canvas.gc

    graphicsContext.fill(Color.brown)
    val fill0 = gc.fill
    assertThat(fill0).isInstanceOf(javafx.scene.paint.Color::class.java)
    graphicsContext.fill(Color.brown)

    //Should be the same! No new Color should be instantiated
    assertThat(gc.fill).isSameAs(fill0)
  }

  @Test
  /**
   * If this test fails on linux, most likely the font Arial is not installed.
   * On Ubuntu this can be accomplished via
   * sudo apt-get install ttf-mscorefonts-installer
   */
  internal fun testFontWidth() {
    val canvas = CanvasFX(type = CanvasType.OffScreen)
    val gc = canvas.canvas.graphicsContext2D
    val graphicsContext = canvas.gc

    val text = "Asdf"

    FontDescriptor(FontFamily("Arial"), FontSize(16.0)).also {
      graphicsContext.font = it
      assertThat(graphicsContext.calculateTextWidth(text)).isCloseTo(32.0, 0.1)
    }

    FontDescriptor(FontFamily("Arial"), size = FontSize(20.0)).also {
      graphicsContext.font = it
      assertThat(graphicsContext.calculateTextWidth(text)).isCloseTo(40.0, 0.1)
    }
  }

  @Test
  internal fun testFontReverse() {
    val context = CanvasRenderingContextFX(CanvasFX(type = CanvasType.OffScreen))
    assertThat(context.font).isEqualTo(corporateDesign.textFont)

    FontDescriptor(FontFamily("Arial"), size = FontSize(99.0)).also {
      context.font = it
      assertThat(context.font).isSameAs(it)
    }
  }

  @Test
  internal fun testSaveRestore() {
    val context = CanvasRenderingContextFX(CanvasFX(type = CanvasType.OffScreen))
    assertThat(context.font).isEqualTo(corporateDesign.textFont)

    val font1 = FontDescriptor(FontFamily("BlaFasel"), size = FontSize(109.0))


    assertThat(context.font).isEqualTo(corporateDesign.textFont)
    context.font = font1
    assertThat(context.font).isEqualTo(font1)


    //No save the context and change the font
    context.saved { savedContext ->
      FontDescriptor(FontFamily("Arial my test"), size = FontSize(24.0)).also {
        assertThat(context.font).isEqualTo(font1)
        savedContext.font = it
        assertThat(savedContext.font).isEqualTo(it)
        assertThat(savedContext.font).isSameAs(it)
      }
    }

    //Restore --> Font is restored
    assertThat(context.font).isEqualTo(font1)
  }
}
