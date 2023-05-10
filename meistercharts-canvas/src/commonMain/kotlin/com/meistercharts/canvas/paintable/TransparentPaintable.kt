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
package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size

/**
 * A paintable that has a given size, but does not paint anything
 */
class TransparentPaintable(val size: Size) : Paintable {
  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle = Rectangle(Coordinates.origin, Size(size.width, size.height))

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
  }
}
