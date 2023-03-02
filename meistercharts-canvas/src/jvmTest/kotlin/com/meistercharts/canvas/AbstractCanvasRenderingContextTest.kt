package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import com.meistercharts.canvas.mock.MockCanvasRenderingContext
import org.junit.jupiter.api.Test

class AbstractCanvasRenderingContextTest {
  @Test
  fun testSnapTranslation() {
    val renderingContext = MockCanvasRenderingContext()

    assertThat(renderingContext.scaleX).isEqualTo(1.0)
    assertThat(renderingContext.translationX).isEqualTo(0.0)

    renderingContext.translationX = 17.5
    assertThat(renderingContext.translationX).isEqualTo(17.5)
    renderingContext.snapPhysicalTranslation()
    assertThat(renderingContext.translationX).isEqualTo(18.0)

    renderingContext.translationX = 17.4
    assertThat(renderingContext.translationX).isEqualTo(17.4)
    renderingContext.snapPhysicalTranslation()
    assertThat(renderingContext.translationX).isEqualTo(17.0)
  }
}
