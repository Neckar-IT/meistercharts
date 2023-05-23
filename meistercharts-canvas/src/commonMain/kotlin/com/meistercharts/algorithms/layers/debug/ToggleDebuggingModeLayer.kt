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
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.debug
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.KeyCode
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.ModifierCombination
import it.neckar.open.kotlin.lang.toggle

/**
 * A layer that toggles the debugging mode
 */
class ToggleDebuggingModeLayer : AbstractLayer() {
  val configuration: Configuration = Configuration()

  /**
   * Move to background to ensure this layer is the last layer that receives the key events
   */
  override val type: LayerType = LayerType.Background

  override fun paint(paintingContext: LayerPaintingContext) {
  }

  /**
   * Listen to the "magic" debug keystroke
   */
  override val keyEventHandler: CanvasKeyEventHandler = object : CanvasKeyEventHandler {
    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      when (event.keyStroke) {
        configuration.toggleDebugKeyStroke -> {
          chartSupport.debug.toggle()
          chartSupport.layerSupport.markAsDirty(DirtyReason.UserInteraction)

          return EventConsumption.Consumed
        }

        configuration.toggleRecordPaintStatisticsKeyStroke -> {
          chartSupport.layerSupport::recordPaintStatistics.toggle()
          chartSupport.layerSupport.markAsDirty(DirtyReason.UserInteraction)

          return EventConsumption.Consumed
        }

        else -> return EventConsumption.Ignored
      }

    }
  }

  @ConfigurationDsl
  class Configuration {
    val toggleDebugKeyStroke: KeyStroke = KeyStroke(KeyCode('D'), ModifierCombination.CtrlShiftAlt)
    val toggleRecordPaintStatisticsKeyStroke: KeyStroke = KeyStroke(KeyCode('P'), ModifierCombination.CtrlShiftAlt)
  }
}
