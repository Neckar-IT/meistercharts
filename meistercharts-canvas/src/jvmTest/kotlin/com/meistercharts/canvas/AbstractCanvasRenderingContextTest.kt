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
