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
package com.meistercharts.algorithms.layers.toolbar

import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.color.ColorProviderNullable
import it.neckar.geometry.Size

/**
 * Default implementation that
 */
class DefaultButtonPaintableProvider(
  /**
   * Returns the paintable for the given size and fill
   */
  val paintableResolver: (size: Size, fill: ColorProviderNullable) -> Paintable,

  val sizeProvider: (state: ButtonState) -> Size,
  val fillProvider: (state: ButtonState) -> ColorProviderNullable,
) {

  /**
   * Returns the paintable for the given state using the [paintableResolver]
   */
  fun getPaintable(buttonState: ButtonState): Paintable {
    val size = sizeProvider(buttonState)
    val fill = fillProvider(buttonState)

    return paintableResolver(size, fill)
  }
}
