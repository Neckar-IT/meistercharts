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

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.EventConsumption.Ignored
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.matches

/**
 * layer that toggles the visibility on shortcut (Ctrl-Alt-V)
 *
 */
class LayerVisibilityToggleOnShortcut<T : Layer>(
  val delegate: LayerVisibilityAdapterWithState<T>,
  val keyStroke: KeyStroke,
) : Layer by delegate {

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    delegate.layout(paintingContext)
  }

  override val keyEventHandler: CanvasKeyEventHandler? = object : CanvasKeyEventHandler {
    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      if (!keyStroke.matches(event)) {
        return Ignored
      }

      delegate.toggleVisibility()
      chartSupport.markAsDirty()
      return Consumed
    }
  }
}

/**
 * Wraps the layer into an [LayerVisibilityAdapter]
 */
fun <T : Layer> LayerVisibilityAdapterWithState<T>.toggleShortcut(keyStroke: KeyStroke): LayerVisibilityToggleOnShortcut<T> {
  return LayerVisibilityToggleOnShortcut(this, keyStroke)
}
