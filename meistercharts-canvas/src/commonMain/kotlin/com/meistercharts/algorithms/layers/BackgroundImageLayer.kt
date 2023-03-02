package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.paintable.ObjectFit
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.design.Theme
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction

/**
 * Shows a background image - in the window
 */
class BackgroundImageLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  val style: Style = Style().also(styleConfiguration)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.fill(style.background)
    gc.fillRect(gc.boundingBox)

    style.backgroundImage?.let {
      val imageSize = it.boundingBox(paintingContext).size
      val boundingBoxSize = gc.canvasSize.containWithAspectRatio(imageSize.aspectRatio)

      it.paintInBoundingBox(paintingContext, Coordinates.of(0.0, gc.height), Direction.BottomLeft, boundingBoxSize, ObjectFit.Contain)
    }
  }

  class Style {
    /**
     * The color to be used as background
     */
    var background: Color = Theme.lightBackgroundColor()

    /**
     * The optional background image that is painted in origin.
     * The paintable is *not* resized
     */
    var backgroundImage: Paintable? = null

    /**
     * Switches to the light background color
     */
    fun light() {
      background = Theme.lightBackgroundColor()
    }

    fun dark() {
      background = Theme.darkBackgroundColor()
    }
  }

}
