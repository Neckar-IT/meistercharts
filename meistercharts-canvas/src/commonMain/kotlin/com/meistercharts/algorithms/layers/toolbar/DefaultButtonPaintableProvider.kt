package com.meistercharts.algorithms.layers.toolbar

import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.color.ColorProviderNullable
import it.neckar.geometry.Size

/**
 * Default implementation that
 */
class DefaultButtonPaintableProvider(
  /**
   * Returns the paintable for the given size and fill
   */
  val paintableResolver: (size: Size, fill: ColorProviderNullable) -> Paintable,

  val sizeProvider: (state: ButtonState) -> Size,
  val fillProvider: (state: ButtonState) -> ColorProviderNullable,
) {

  /**
   * Returns the paintable for the given state using the [paintableResolver]
   */
  fun getPaintable(buttonState: ButtonState): Paintable {
    val size = sizeProvider(buttonState)
    val fill = fillProvider(buttonState)

    return paintableResolver(size, fill)
  }
}
