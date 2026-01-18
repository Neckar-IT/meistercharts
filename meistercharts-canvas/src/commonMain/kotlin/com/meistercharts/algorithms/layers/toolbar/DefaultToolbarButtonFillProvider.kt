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
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProviderNullable
import it.neckar.open.kotlin.lang.asProvider

/**
 * Provides a fixed color depending on the state
 */
class DefaultToolbarButtonFillProvider {
  fun color(state: ButtonState): ColorProviderNullable {
    return when {
      state.disabled -> Color.rgba(200, 200, 200, 0.6).asProvider()
      state.pressed -> Color.rgba(150, 150, 150, 1.0).asProvider()
      state.hover -> Color.rgba(150, 150, 150, 0.75).asProvider()
      state.focused -> Color.rgba(150, 150, 150, 0.85).asProvider()
      else -> Color.rgba(150, 150, 150, 0.6).asProvider()
    }
  }
}
