package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.canvas.saved
import com.meistercharts.model.Direction
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px

/**
 * Shows some debug markers for the content area
 */
open class ContentAreaDebugLayer(
  styleConfiguration: Style.() -> Unit = {},
) : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  val style: Style = Style().also(styleConfiguration)

  override fun paint(paintingContext: LayerPaintingContext) {
    val layerSupport = paintingContext.layerSupport
    val calculator = paintingContext.chartCalculator
    val gc = paintingContext.gc
    val snapSupport = paintingContext.chartSupport.pixelSnapSupport

    //Outer line
    gc.lineWidth = 2.0
    gc.strokeStyle(style.line0pct)
    strokeRect(gc, calculator, 0.0, 0.0)

    //5px inner line
    gc.lineWidth = 1.0
    gc.strokeStyle(style.line5px)
    strokeRect(gc, calculator, 0.0, 5.5)

    //10% inner line
    gc.lineWidth = 1.0
    gc.strokeStyle(style.line10pct)
    strokeRect(gc, calculator, 0.1, 0.0)

    //25% inner line
    gc.lineWidth = 1.0
    gc.strokeStyle(style.line25pct)
    strokeRect(gc, calculator, 0.25, 0.0)


    //diagonals
    @Window @px val x0 = calculator.contentAreaRelative2windowX(0.0)
    @Window @px val y0 = calculator.contentAreaRelative2windowY(0.0)

    @Window @px val x1 = calculator.contentAreaRelative2windowX(1.0)
    @Window @px val y1 = calculator.contentAreaRelative2windowY(1.0)

    gc.strokeStyle(style.diagonals)
    gc.strokeLine(x0, y0, x1, y1)
    gc.strokeLine(x0, y1, x1, y0)

    //Draw the texts
    gc.saved {
      gc.translate(x0, y0)
      gc.paintTextBox("@ContentArea: 0/0", Direction.TopLeft, 0.0, 0.0, BoxStyle.gray, Color.white)
    }

    gc.saved {
      gc.translate(x1, y1)
      gc.paintTextBox("@ContentArea: 1.0/1.0", Direction.BottomRight, 0.0, 0.0, BoxStyle.gray, Color.white)
    }

    //Paint helper lines if the complete content area is invisible!
    if (x0 > gc.width || x1 < 0.0) {
      gc.setLineDash(2.0, 3.0)
      //Paint y lines
      gc.strokeStyle(style.line0pct)
      gc.strokeLine(0.0, y0, gc.width, y0)
      gc.strokeLine(0.0, y1, gc.width, y1)

      gc.strokeStyle(style.line5px)
      gc.strokeLine(0.0, y0 + 5.0, gc.width, y0 + 5.0)
      gc.strokeLine(0.0, y1 - 5.0, gc.width, y1 - 5.0)
    }

    if (y0 > gc.height || y1 < 0.0) {
      gc.setLineDash(2.0, 3.0)

      //Paint x lines
      gc.strokeStyle(style.line0pct)
      gc.strokeLine(x0, 0.0, x0, gc.height)
      gc.strokeLine(x1, 0.0, x1, gc.height)

      gc.strokeStyle(style.line5px)
      gc.strokeLine(x0 + 5.0, 0.0, x0 + 5.0, gc.height)
      gc.strokeLine(x1 - 5.0, 0.0, x1 - 5.0, gc.height)
    }
  }

  private fun strokeRect(gc: CanvasRenderingContext, calculator: ChartCalculator, @pct inRelative: Double, @px inAbsolute: Double) {
    @Window @px val x0 = calculator.contentAreaRelative2windowX(0 + inRelative) + inAbsolute
    @Window @px val y0 = calculator.contentAreaRelative2windowY(0 + inRelative) + inAbsolute

    @Zoomed @px val width = calculator.contentAreaRelative2zoomedX(1 - 2 * inRelative) - 2 * inAbsolute
    @Zoomed @px val height = calculator.contentAreaRelative2zoomedY(1 - 2 * inRelative) - 2 * inAbsolute

    //Outer line
    gc.strokeRect(x0, y0, width, height)
  }


  @StyleDsl
  class Style {
    /**
     * Sets all lines to the given color
     */
    fun lines(color: Color) {
      line0pct = color
      line5px = color
      line10pct = color
      line25pct = color
      diagonals = color
    }

    /**
     * The outer line
     */
    var line0pct: Color = Color.red

    /**
     * The 5px line
     */
    var line5px: Color = Color.red

    /**
     * The 10% line
     */
    var line10pct: Color = Color.orange

    /**
     * The 25% line
     */
    var line25pct: Color = Color.orange

    /**
     * The diagonals
     */
    var diagonals: Color = Color.orange

    var font: FontDescriptorFragment = FontDescriptorFragment.empty
  }
}
