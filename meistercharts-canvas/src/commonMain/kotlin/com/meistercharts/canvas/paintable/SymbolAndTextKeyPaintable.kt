package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.PaintableLocation
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.paintTextWithPaintable
import com.meistercharts.canvas.textService
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.resolve
import com.meistercharts.style.Palette
import it.neckar.open.unit.other.px
import kotlin.math.max

/**
 * A [Paintable] that displays a symbol and a text
 *
 * The logical center lies between the symbol and the text.
 */
class SymbolAndTextKeyPaintable(
  val symbol: Paintable,
  val text: TextKey,
  styleConfiguration: Style.() -> Unit = {}
) : Paintable {

  val style: Style = Style().also(styleConfiguration)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    val chartSupport = paintingContext.chartSupport

    val symbolBoundingBox = symbol.boundingBox(paintingContext)
    val gc = chartSupport.canvas.gc
    gc.font(style.textFont)
    val textWidthForLayout = style.textWidthForLayout ?: gc.calculateTextWidth(text.resolve(chartSupport.textService, chartSupport.i18nConfiguration))
    val size = Size(symbolBoundingBox.getWidth() + style.gap + textWidthForLayout, max(symbolBoundingBox.getHeight(), style.textHeightForLayout))

    return Rectangle(Coordinates(-symbolBoundingBox.getWidth() - style.gap / 2.0, -size.height / 2.0), size)
  }

  override fun paint(
    paintingContext: LayerPaintingContext,
    x: @Window Double,
    y: @Window Double,
  ) {
    val gc = paintingContext.gc
    gc.font(style.textFont)
    gc.fill(style.textColor)
    gc.translate(x, y)
    val text = text.resolve(paintingContext.chartSupport.textService, paintingContext.chartSupport.i18nConfiguration)
    paintingContext.paintTextWithPaintable(text, symbol, style.symbolLocation, Direction.Center, gap = style.gap, maxTextWidth = style.textWidthForLayout)
  }

  @StyleDsl
  open class Style {
    /**
     * Where to paint the symbol in relation to the text
     */
    var symbolLocation: PaintableLocation = PaintableLocation.PaintableLeft

    /**
     * The gap between the symbol and the text
     */
    var gap: @px Double = 5.0

    /**
     * The color the text is painted with
     */
    var textColor: Color = Palette.defaultGray

    /**
     * The font the text is painted with
     */
    var textFont: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * The maximum width of the text in pixels.
     */
    @px
    var textWidthForLayout: Double? = null

    /**
     * The height of the text. This is required to calculate the layout
     */
    var textHeightForLayout: Double = 15.0
  }
}

