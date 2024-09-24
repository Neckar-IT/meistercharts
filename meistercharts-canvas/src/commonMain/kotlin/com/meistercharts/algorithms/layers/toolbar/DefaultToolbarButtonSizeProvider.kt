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
