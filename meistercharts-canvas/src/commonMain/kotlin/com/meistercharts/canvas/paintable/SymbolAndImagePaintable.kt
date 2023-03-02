package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.saved
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.style.Palette
import it.neckar.open.unit.other.px
import kotlin.math.max

/**
 * A [Paintable] that displays a symbol and an image.
 *
 * So far the symbol is always to the left of the image.
 *
 * The logical center lies between the symbol and the image.
 */
class SymbolAndImagePaintable(
  val symbol: Paintable,
  val image: Paintable,
  styleConfiguration: Style.() -> Unit = {}
) : Paintable {

  val style: Style = Style().also(styleConfiguration)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    val chartSupport = paintingContext.chartSupport

    val symbolBoundingBox = symbol.boundingBox(paintingContext)
    val size = Size(symbolBoundingBox.getWidth() + style.gapHorizontal + style.imageSize.width, max(symbolBoundingBox.getHeight(), style.imageSize.height))
    return Rectangle(-symbolBoundingBox.getWidth() - style.gapHorizontal / 2.0, -size.height / 2.0, size.width, size.height)
  }


  override fun paint(
    paintingContext: LayerPaintingContext,
    x: @Window Double,
    y: @Window Double,
  ) {
    val boundingBox = boundingBox(paintingContext)

    paintingContext.gc.saved {
      symbol.paintInBoundingBox(paintingContext, x, y, Direction.CenterRight, style.gapHorizontal / 2.0, style.gapVertical / 2.0, boundingBox.getWidth(), boundingBox.getHeight())
    }
    image.paintInBoundingBox(paintingContext, x, y, Direction.CenterLeft, style.gapHorizontal / 2.0, style.gapVertical / 2.0, boundingBox.getWidth(), boundingBox.getHeight())
  }

  @StyleDsl
  open class Style {
    /**
     * The size of the image
     */
    var imageSize: Size = Size.PX_24

    /**
     * The color the image should be painted in
     */
    var imageColor: Color = Palette.defaultGray

    /**
     * The gap between the symbol and the image
     */
    var gapHorizontal: @px Double = 5.0
    var gapVertical: @px Double = 5.0
  }
}
