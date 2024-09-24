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
