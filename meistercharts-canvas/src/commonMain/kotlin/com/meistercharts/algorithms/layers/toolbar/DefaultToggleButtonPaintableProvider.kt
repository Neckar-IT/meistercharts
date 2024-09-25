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
 * Provides the paintables for a toggle button
 */
class DefaultToggleButtonPaintableProvider(
  /**
   * Returns the paintable for the given size and fill
   */
  val defaultPaintableResolver: ButtonPaintableResolver,
  /**
   * Returns the paintable for the selected state.
   * If the same icon is used for selected and unselected states, this can be the same as [defaultPaintableResolver].
   */
  val selectedPaintableResolver: ButtonPaintableResolver = defaultPaintableResolver,

  /**
   * Returns the size for the given state
   */
  val sizeProvider: (state: ButtonState) -> Size,

  /**
   * Returns the fill for the given state
   */
  val fillProvider: (state: ButtonState) -> ColorProviderNullable,
) {

  /**
   * Returns the paintable for the given state
   */
  fun getPaintable(buttonState: ButtonState): Paintable {
    val size = sizeProvider(buttonState)
    val fill = fillProvider(buttonState)

    return if (buttonState.selected) {
      selectedPaintableResolver(size, fill)
    } else {
      defaultPaintableResolver(size, fill)
    }
  }
}
