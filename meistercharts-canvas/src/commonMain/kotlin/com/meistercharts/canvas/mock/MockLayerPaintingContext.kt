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
package com.meistercharts.canvas.mock

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DefaultLayerSupport
import com.meistercharts.canvas.MockCanvas
import com.meistercharts.canvas.PaintingLoopIndex
import it.neckar.open.unit.si.ms

object MockLayerPaintingContext {
  /**
   * Returns a new instance
   */
  operator fun invoke(frameTimestamp: @ms Double = 10.0, frameTimestampDelta: @ms Double = 0.0, loopIndex: PaintingLoopIndex = PaintingLoopIndex(0)): LayerPaintingContext {
    return LayerPaintingContext(MockCanvasRenderingContext(), DefaultLayerSupport(ChartSupport(MockCanvas())), frameTimestamp, frameTimestampDelta, loopIndex)
  }
}
