package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.calculateOffsetXForGap
import com.meistercharts.canvas.calculateOffsetYForGap
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.textService
import com.meistercharts.model.Direction
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Rectangle
import com.meistercharts.model.VerticalAlignment
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.unit.other.px

/**
 * Paints a single text.
 *
 * If the provided text is null or blank, the bounding box of this paintable is [Rectangle.zero]
 */
class LabelPaintable(
  label: (textService: TextService, i18nConfiguration: I18nConfiguration) -> String?,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractPaintable() {
  val configuration: Configuration = Configuration(label).also(additionalConfiguration)

  override fun paintingVariables(): PaintablePaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : PaintablePaintingVariablesImpl() {
    var label: String? = null

    override fun calculate(paintingContext: LayerPaintingContext) {
      super.calculate(paintingContext)

      val gc = paintingContext.gc
      gc.font(configuration.font)
      val fontMetrics = gc.getFontMetrics()
      @px val lineHeight = fontMetrics.totalHeight

      val chartSupport = paintingContext.chartSupport
      val label = configuration.label(chartSupport.textService, chartSupport.i18nConfiguration)
      this.label = label

      if (label.isNullOrEmpty()) {
        boundingBox = Rectangle(0.0, 0.0, 0.0, lineHeight) //always has the height of the text
        this.label = null
        return
      }

      @px val textWidth = gc.calculateTextWidth(label).coerceAtMost(configuration.maxWidth)

      val anchorDirection = configuration.anchorDirection

      val relevantX = anchorDirection.calculateOffsetXForGap(configuration.anchorGapHorizontal)
      val relevantY = anchorDirection.calculateOffsetYForGap(configuration.anchorGapVertical)

      @px val left = relevantX + when (anchorDirection.horizontalAlignment) {
        HorizontalAlignment.Left -> 0.0
        HorizontalAlignment.Center -> -textWidth / 2.0
        HorizontalAlignment.Right -> -textWidth
      }

      @px val top = relevantY + when (anchorDirection.verticalAlignment) {
        VerticalAlignment.Top -> 0.0
        VerticalAlignment.Center -> -lineHeight / 2.0
        VerticalAlignment.Baseline -> -fontMetrics.accentLine
        VerticalAlignment.Bottom -> -lineHeight
      }

      //TODO! These calculations seem to be duplicate with paintTextBox. Maybe somehow extract these values

      boundingBox = Rectangle(left, top, textWidth, lineHeight)
    }
  }

  override fun paintAfterLayout(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val label = paintingVariables.label
    if (label.isNullOrEmpty()) {
      return
    }

    val gc = paintingContext.gc

    gc.fill(configuration.labelColor())

    gc.font(configuration.font)
    gc.fillText(label, x, y, configuration.anchorDirection, configuration.anchorGapHorizontal, configuration.anchorGapVertical, configuration.maxWidth)
  }

  class Configuration(
    var label: (textService: TextService, i18nConfiguration: I18nConfiguration) -> String?,
  ) {

    var font: FontDescriptorFragment = FontDescriptorFragment.DefaultSize
    var labelColor: () -> Color = Color.black.asProvider()

    var anchorDirection: Direction = Direction.TopLeft
    var anchorGapHorizontal: @px Double = 0.0
    var anchorGapVertical: @px Double = 0.0
    var maxWidth: @px Double = Double.NaN
  }
}
