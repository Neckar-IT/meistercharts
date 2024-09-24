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
