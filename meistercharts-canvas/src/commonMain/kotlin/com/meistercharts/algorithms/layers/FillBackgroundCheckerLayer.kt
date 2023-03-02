package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Zoomed
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.kotlin.lang.toIntCeil

/**
 * Fills the canvas with a background checker pattern
 */
class FillBackgroundCheckerLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  val style: Style = Style().also(styleConfiguration)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val cols = (gc.width / style.segmentWith).toIntCeil()
    val rows = (gc.height / style.segmentHeight).toIntCeil()

    cols.fastFor { col ->
      rows.fastFor { row ->
        gc.fill(
          if ((col + row) % 2 == 0) style.background0 else style.background1
        )

        gc.fillRect(col * style.segmentWith, row * style.segmentHeight, style.segmentWith, style.segmentHeight)
      }
    }
  }

  class Style {
    /**
     * The color to be used as background
     */
    var background0: Color = Color.lightgray
    var background1: Color = Color.white

    var segmentWith: @Zoomed Double = 15.0
    var segmentHeight: @Zoomed Double = 15.0
  }
}

/**
 * Adds a background layer with checkers
 */
fun Layers.addBackgroundChecker(): FillBackgroundCheckerLayer {
  return FillBackgroundCheckerLayer().also {
    addLayer(it)
  }
}
