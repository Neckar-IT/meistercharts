package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.pixelSnapSupport

/**
 * Shows some debug markers for the current window
 */
class WindowDebugLayer(
  override val type: LayerType = LayerType.Content
) : AbstractLayer() {
  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val snapSupport = paintingContext.chartSupport.pixelSnapSupport

    val width = gc.width
    val height = gc.height

    //Around
    gc.beginPath()
    gc.rect(snapSupport.snapXValue(0.0), snapSupport.snapYValue(0.0), snapSupport.snapXSize(width), snapSupport.snapYSize(height))
    gc.closePath()
    gc.stroke(Color.orangered)

    gc.lineWidth = 3.0
    gc.stroke()

    //cross wire to mark the center
    gc.beginPath()
    gc.moveTo(snapSupport.snapXValue(0.0), snapSupport.snapYValue(height / 2.0))
    gc.lineTo(snapSupport.snapXValue(width), snapSupport.snapYValue(height / 2.0))
    gc.moveTo(snapSupport.snapXValue(width / 2.0), 0.0)
    gc.lineTo(snapSupport.snapXValue(width / 2.0), snapSupport.snapYValue(height))

    gc.closePath()
    gc.lineWidth = 2.0
    gc.stroke()

    //oval in the corners
    //Bottom right

    gc.strokeOvalOrigin(snapSupport.snapXValue(width - 10), snapSupport.snapYValue(height - 10), snapSupport.snapXSize(20.0), snapSupport.snapYSize(20.0))
    //Top right
    gc.strokeOvalOrigin(snapSupport.snapXValue(width - 10), snapSupport.snapYValue((0 - 10).toDouble()), snapSupport.snapXSize(20.0), snapSupport.snapYSize(20.0))
    //Bottom left
    gc.strokeOvalOrigin(snapSupport.snapXValue((0 - 10).toDouble()), snapSupport.snapYValue(height - 10), snapSupport.snapXSize(20.0), snapSupport.snapYSize(20.0))
    //top left
    gc.strokeOvalOrigin(snapSupport.snapXValue((0 - 10).toDouble()), snapSupport.snapYValue((0 - 10).toDouble()), snapSupport.snapXSize(20.0), snapSupport.snapYSize(20.0))

  }
}
