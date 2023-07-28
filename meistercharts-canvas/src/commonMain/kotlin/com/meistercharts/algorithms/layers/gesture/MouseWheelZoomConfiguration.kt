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
package com.meistercharts.algorithms.layers.gesture

import it.neckar.geometry.AxisSelection
import it.neckar.events.ModifierCombination

/**
 * Configuration for the mouse wheel zoom
 */
data class MouseWheelZoomConfiguration(
  /**
   * Which axis can be zoomed
   */
  var zoomAxisSelection: AxisSelection = AxisSelection.Both,

  /**
   * Which modifier must be set to zoom along both axes
   */
  var zoomXandYModifier: ModifierCombination? = ModifierCombination.CtrlShift,

  /**
   * Which modifier must be set to zoom along the x-axis
   */
  var zoomXModifier: ModifierCombination? = ModifierCombination.Control,

  /**
   * Which modifier must be set to zoom along the y-axis
   */
  var zoomYModifier: ModifierCombination? = ModifierCombination.Shift,
) {

  companion object {
    /**
     * Supports zooming without modifier
     */
    val withoutModifiers: MouseWheelZoomConfiguration = MouseWheelZoomConfiguration(zoomXandYModifier = ModifierCombination.None)

    /**
     * Supports zooming with both axis only
     */
    val bothAxis: MouseWheelZoomConfiguration = MouseWheelZoomConfiguration(
      zoomXandYModifier = ModifierCombination.None,
      zoomXModifier = null,
      zoomYModifier = null
    )
  }
}
