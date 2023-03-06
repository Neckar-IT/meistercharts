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
package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.paintable.ObjectFit
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Direction

/**
 * Paints a paintable in the content area
 */
class PaintableInContentAreaLayer(var backgroundImage: Paintable) : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val chartCalculator = paintingContext.chartCalculator

    val x = chartCalculator.contentAreaRelative2windowX(0.0)
    val y = chartCalculator.contentAreaRelative2windowY(0.0)

    val width = chartCalculator.contentAreaRelative2zoomedX(1.0)
    val height = chartCalculator.contentAreaRelative2zoomedY(1.0)

    backgroundImage.paintInBoundingBox(paintingContext, x, y, Direction.TopLeft, 0.0, 0.0, width, height, ObjectFit.Contain)
  }
}
