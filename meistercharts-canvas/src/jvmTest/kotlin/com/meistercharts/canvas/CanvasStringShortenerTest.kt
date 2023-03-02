package com.meistercharts.canvas

import com.meistercharts.canvas.mock.MockCanvasRenderingContext
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test

class CanvasStringShortenerTest {
  @Test
  fun testIt() {
    val canvasStringShortener = CanvasStringShortener.ExactButVerySlow(StringShortener.TruncateToLength)

    val gc = MockCanvasRenderingContext()
    gc.mockTextWidth = 100.0

    canvasStringShortener.shorten("asdfasdf", 123.0, gc).let {
      assertThat(it).isNotNull()
      assertThat(it).isEqualTo("asdfasdf")
    }

    gc.mockTextWidth = 200.0
    canvasStringShortener.shorten("asdfasdf", 123.0, gc).let {
      assertThat(it).isNull()
    }
  }

  @Test
  fun testSmall() {
    val gc = MockCanvasRenderingContext()
    gc.mockTextWidth = 200.0

    val faster = CanvasStringShortener.ExactButSlow(StringShortener.TruncateToLength)
    assertThat(faster.shorten("aa", 123.0, gc)).isNull()
  }

  @Test
  fun testIntervalHalf() {
    val slow = CanvasStringShortener.ExactButVerySlow(StringShortener.TruncateToLength)
    val faster = CanvasStringShortener.ExactButSlow(StringShortener.TruncateToLength)

    val gc = MockCanvasRenderingContext()
    gc.mockTextWidth = 100.0

    "asdfasdf".let { text ->
      slow.shorten(text, 123.0, gc).let { shortened ->
        assertThat(shortened).isNotNull()
        assertThat(shortened).isEqualTo("asdfasdf")

        assertThat(faster.shorten(text, 123.0, gc)).isEqualTo(shortened)
      }
    }

    gc.mockTextWidth = 200.0
    slow.shorten("asdfasdf", 123.0, gc).let {
      assertThat(it).isNull()
      assertThat(faster.shorten("aa", 123.0, gc)).isNull()
    }
  }

  @Test
  fun testNoOp() {
    val gc: CanvasRenderingContext = MockCanvasRenderingContext()
    assertThat(CanvasStringShortener.NoOp.shorten("123123123", 1.0, gc)).isEqualTo("123123123")
  }
}
