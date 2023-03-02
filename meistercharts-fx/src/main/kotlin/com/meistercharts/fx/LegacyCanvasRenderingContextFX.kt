package com.meistercharts.fx

import com.meistercharts.canvas.Canvas
import com.meistercharts.model.Size
import com.meistercharts.fx.BaseCanvasRenderingContextFX
import javafx.scene.canvas.GraphicsContext

/**
 * FX canvas rendering context that just uses a graphics context.
 * Only used for legacy components
 *
 */
class LegacyCanvasRenderingContextFX(
  override val context: GraphicsContext
) : BaseCanvasRenderingContextFX() {

  override val canvas: Canvas
    get() {
      throw UnsupportedOperationException("Canvas is not available for legacy context")
    }

  init {
    applyDefaults()
  }

  override val canvasSize: Size
    get() {
      return Size(width, height)
    }

  override val width: Double
    get() {
      return context.canvas.width
    }
  override val height: Double
    get() {
      return context.canvas.height
    }
}
