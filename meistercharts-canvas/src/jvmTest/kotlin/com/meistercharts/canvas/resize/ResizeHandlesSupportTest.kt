/*
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
package com.meistercharts.canvas.resize

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.loop.PaintingLoopIndex
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.geometry.Rectangle
import org.junit.jupiter.api.Test

class ResizeHandlesSupportTest {

  private val support: ResizeHandlesSupport = ResizeHandlesSupport()

  private val layer: Layer = TestLayer()

  private val handler: RecordingResizeHandler = RecordingResizeHandler()

  private val loopIndex: PaintingLoopIndex = PaintingLoopIndex(0)

  @Test
  fun `clearing takes the handles away`() {
    register()
    assertThat(support.resizableContentBounds(loopIndex)).isNotNull()

    support.clear(layer)

    assertThat(support.resizableContentBounds(loopIndex)).isNull()
  }

  /**
   * Deleting the resized element clears the resizable while the pointer is still down. The gesture ends one layout
   * later, and that notification has to reach the handler that began it.
   */
  @Test
  fun `a cleared layer is still the one notified`() {
    register()
    support.clear(layer)

    support.notifyResizingFinished()

    assertThat(handler.resizingFinishedCount).isEqualTo(1)
  }

  @Test
  fun `another layer does not clear a foreign resizable`() {
    register()

    support.clear(TestLayer())

    assertThat(support.resizableContentBounds(loopIndex)).isNotNull()
  }

  @Test
  fun `a resizable without a registered handler is refused`() {
    assertFailure { support.setResizable(layer, Rectangle(0.0, 0.0, 10.0, 10.0), loopIndex) }
      .isInstanceOf(IllegalStateException::class)
  }

  /**
   * A layer that is no longer laid out never reaches [ResizeHandlesSupport.clear] - its registration expires instead.
   */
  @Test
  fun `a registration counts for its own paint loop only`() {
    register()

    assertThat(support.resizableContentBounds(loopIndex.next())).isNull()
    assertThat(support.resizableContentBounds(loopIndex)).isNotNull()
  }

  private fun register() {
    support.onResize(layer, handler)
    support.setResizable(layer, Rectangle(0.0, 0.0, 10.0, 10.0), loopIndex)
  }

  private class TestLayer : AbstractLayer() {
    override val type: LayerType = LayerType.Content

    override fun paint(paintingContext: LayerPaintingContext) {
    }
  }

  private class RecordingResizeHandler : ResizeHandler {
    var resizingFinishedCount: Int = 0

    override fun resizing(totalDistance: Distance, handleDirection: Direction, totalX: Double, totalY: Double) {
    }

    override fun resizingFinished() {
      resizingFinishedCount++
    }
  }
}
