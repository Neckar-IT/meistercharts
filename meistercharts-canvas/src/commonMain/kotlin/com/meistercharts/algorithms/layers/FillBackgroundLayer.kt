package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.design.Theme
import com.meistercharts.model.Coordinates

/**
 * Fills the canvas with a background color
 */
class FillBackgroundLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  val style: Style = Style().also(styleConfiguration)

  constructor(backgroundColor: Color) : this({
    this.background = backgroundColor
  })

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.fill(style.background)
    gc.fillRect(gc.boundingBox)

    style.backgroundImage?.paint(paintingContext, Coordinates.origin)
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
