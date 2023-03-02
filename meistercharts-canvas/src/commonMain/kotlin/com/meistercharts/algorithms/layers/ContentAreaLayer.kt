package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.ContentAreaPainter
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.model.SidesSelection
import it.neckar.open.unit.other.px

/**
 * Strokes lines around the content are
 */
class ContentAreaLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  private val contentAreaPainter = ContentAreaPainter()

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.lineWidth = style.lineWidth

    contentAreaPainter
      .also {
        it.stroke = style.color
        it.sidesToPaint = style.sidesToPaint
      }
      .paint(gc, paintingContext.chartCalculator)
  }

  @StyleDsl
  open class Style {
    /**
     * The color for the lines
     */
    var color: Color = Color.silver

    var lineWidth: @px Double = 1.0

    /**
     * Which sides to paint
     */
    var sidesToPaint: SidesSelection = SidesSelection.all
  }
}
