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
import com.meistercharts.algorithms.layers.Layer
import it.neckar.open.time.nowMillis
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

/**
 */
class LayerSupportApiTest {
  @Test
  internal fun name() {
    val mockCanvas = MockCanvas()

    val layerSupport = ChartSupport(mockCanvas)

    layerSupport.markAsDirty(DirtyReason.Unknown)
    layerSupport.disabled = true
  }

  @Test
  fun testEnsurePaintNotCalled() {
    val mockLayer = mockk<Layer> {
      every {
        paint(any())
      } throws AssertionError("must not be called")
    }

    val chartSupport = ChartSupport(MockCanvas())
    val layerSupport = chartSupport.layerSupport
    layerSupport.layers.addLayer(mockLayer)

    assertThat(chartSupport.dirtySupport.dirty).isTrue() //initially set to true
    //reset dirty marker
    chartSupport.dirtySupport.clearIsDirty()

    //Not marked as dirty
    assertThat(chartSupport.dirtySupport.dirty).isFalse()
    chartSupport.render(nowMillis(), 1.0)

    chartSupport.markAsDirty(DirtyReason.Unknown)
    chartSupport.disabled = true

    //Repaint is disabled
    chartSupport.render(nowMillis(), 2.0)

    verify(exactly = 0) {
      mockLayer.paint(any())
    }
  }
}
