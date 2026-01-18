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
package com.meistercharts.algorithms.layers.toolbar

import com.meistercharts.canvas.paintable.ButtonState
import it.neckar.geometry.Size

/**
 * Default implementation for button size provider
 */
class DefaultToolbarButtonSizeProvider(
  var defaultSize: Size = Size.PX_40,
  var activeSize: Size = Size.PX_50,
) {
  fun size(state: ButtonState): Size {
    if (state.disabled) {
      return defaultSize
    }

    return when {
      state.pressed -> activeSize
      state.hover -> activeSize
      else -> defaultSize
    }
  }
}
