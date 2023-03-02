package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import com.meistercharts.model.Zoom
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.i18n.I18nConfiguration
import com.meistercharts.style.BoxStyle

/**
 * Shows the current zoom level
 *
 */
class ShowZoomLevelLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  private val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val text = paintingContext.chartSupport.currentChartState.zoom.getZoomFormatted(paintingContext.i18nConfiguration)

    val gc = paintingContext.gc
    gc.translate(gc.width, 0.0)
    //to top right corner

    gc.font(style.font)
    gc.paintTextBox(text, Direction.TopRight, 5.0, 5.0, style.boxStyle, style.textColor, 150.0)
  }

  /**
   * Returns the zoom as formatted string
   */
  private fun Zoom.getZoomFormatted(i18nConfiguration: I18nConfiguration): String {
    val formattedZoomX = style.decimalFormat.format(scaleX, i18nConfiguration)
    val formattedZoomY = style.decimalFormat.format(scaleY, i18nConfiguration)
    return "X: $formattedZoomX / Y: $formattedZoomY"
  }

  /**
   * The style configuration for a [ShowZoomLevelLayer]
   */
  @StyleDsl
  open class Style {
    /**
     * The style for the box (background fill + border stroke + insets)
     */
    var boxStyle: BoxStyle = BoxStyle(Color.rgba(102, 102, 102, 0.5), null, padding = Insets(3.0, 5.0, 3.0, 5.0))

    /**
     * The color that is used for the text
     */
    var textColor: Color = Color.white

    /**
     * The font that is used for the text
     */
    var font: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * The format that is used to format the zoom-values
     */
    var decimalFormat: CachedNumberFormat = decimalFormat2digits
  }
}

/**
 * Adds a [ShowZoomLevelLayer] to this [Layers]
 */
fun Layers.addShowZoomLevel() {
  addLayer(ShowZoomLevelLayer())
}
