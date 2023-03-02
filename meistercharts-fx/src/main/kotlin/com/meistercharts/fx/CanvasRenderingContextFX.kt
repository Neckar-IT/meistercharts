package com.meistercharts.fx

import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px
import com.meistercharts.fx.BaseCanvasRenderingContextFX
import javafx.scene.canvas.GraphicsContext

/**
 * JavaFX implementation for [CanvasRenderingContext] that uses a [CanvasFX]
 */
class CanvasRenderingContextFX(
  override val canvas: CanvasFX
) : BaseCanvasRenderingContextFX() {

  override val context: GraphicsContext = canvas.canvas.graphicsContext2D

  init {
    applyDefaults()
  }

  @px
  override val canvasSize: Size
    get() = canvas.size

  @px
  override val width: Double
    get() = canvas.width

  @px
  override
  val height: Double
    get() = canvas.height
}

/**
 * Returns the graphics context cast to the platform implementation
 */
fun CanvasRenderingContext.native(): CanvasRenderingContextFX = this as CanvasRenderingContextFX
