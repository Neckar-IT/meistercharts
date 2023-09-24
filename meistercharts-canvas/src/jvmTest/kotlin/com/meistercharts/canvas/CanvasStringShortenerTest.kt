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
package com.meistercharts.canvas

import com.meistercharts.canvas.mock.MockCanvasRenderingContext
import com.meistercharts.canvas.text.CanvasStringShortener
import com.meistercharts.canvas.text.StringShortener
import assertk.*
import assertk.assertions.*

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
