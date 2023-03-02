package com.meistercharts.algorithms.layers.gesture

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.strokeRectCoordinates
import com.meistercharts.model.Coordinates

/**
 * A layer that visualizers the rubber band (mouse gesture)
 */
class RubberBandVisualizationLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val startLocation = data.startLocation() ?: return
    val currentLocation = data.currentLocation() ?: return

    val gc = paintingContext.gc

    gc.stroke(style.stroke)
    gc.fill(style.fill)

    gc.fillRectCoordinates(startLocation, currentLocation)
    gc.strokeRectCoordinates(startLocation, currentLocation)
  }

  class Style {
    /**
     * The stroke of the rubber band
     */
    var stroke: Color = Color.orange

    /**
     * The fill of the rubber band
     */
    var fill: Color = Color.rgba(255, 165, 0, 0.5)
  }

  class Data(
    /**
     * The start location of the rubber band
     */
    val startLocation: () -> Coordinates?,

    /**
     * The current location of the rubber band
     */
    val currentLocation: () -> Coordinates?
  )
}
