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
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.model.Direction

/**
 * A layer that shows the paint count
 */
class PaintCounterLayer : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Notification

  /**
   * Contains the current paint count
   */
  var count: Int = 0

  override fun paint(paintingContext: LayerPaintingContext) {
    count++

    val gc = paintingContext.gc
    gc.stroke(Color.chocolate)
    gc.fillText("Paint count: $count", 10.0, 200.0, Direction.TopLeft)
  }
}
