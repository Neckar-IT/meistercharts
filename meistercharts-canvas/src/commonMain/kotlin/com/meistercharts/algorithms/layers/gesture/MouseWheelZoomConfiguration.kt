package com.meistercharts.algorithms.layers.gesture

import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.events.ModifierCombination

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
