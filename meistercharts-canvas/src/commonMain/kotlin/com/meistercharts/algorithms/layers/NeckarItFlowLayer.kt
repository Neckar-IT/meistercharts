package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.design.neckarit.NeckarItFlowPaintable
import com.meistercharts.model.Coordinates
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px

/**
 * Paints the Neckar IT 'flow'
 */
class NeckarItFlowLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  val style: Style = Style().also(styleConfiguration)

  override
  fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val paintable = NeckarItFlowPaintable.forWidth(gc.width)

    //Translate to center Y
    gc.translate(0.0, gc.centerY + style.gapToCenterY)

    gc.globalAlpha = style.opacity

    paintable.paint(paintingContext, Coordinates.none)
    paintingContext.chartSupport.markAsDirty()


    paintingContext.ifDebug(DebugFeature.ShowBounds) {
      gc.stroke(Color.red)
      gc.strokeRect(paintable.boundingBox(paintingContext))
    }
  }

  class Style {
    /**
     * The gap between the center of the canvas to the top side of the paintable
     */
    @px
    var gapToCenterY: Double = 25.0

    var opacity: @pct Double = 1.0
  }

}
