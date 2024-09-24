package com.meistercharts.algorithms.layers.toolbar

import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.color.ColorProviderNullable
import it.neckar.geometry.Size

/**
 * Provides a [Paintable] for a given button state
 */
typealias ButtonPaintableProvider = (ButtonState) -> Paintable

/**
 * Returns a button paintable based upon the size and fill color
 */
typealias ButtonPaintableResolver = (size: Size, fill: ColorProviderNullable) -> Paintable
