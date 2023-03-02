package com.meistercharts.algorithms.layers

import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import com.meistercharts.resources.Icons

/**
 * Shows a loading icon if a resource is missing
 */
class ShowLoadingIconOnResourceMissingLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  val style: Style = Style().also(styleConfiguration)


  override fun paint(paintingContext: LayerPaintingContext) {
    if (!paintingContext.missingResources.isEmpty()) {
      val gc = paintingContext.gc
      gc.clear()
      style.icon.paintInBoundingBox(paintingContext, gc.centerX, gc.centerY, Direction.Center)
    }
  }

  class Style {
    /**
     * The icon that is painted
     */
    var icon: Paintable = Icons.hourglass(size = Size.PX_60)
  }
}

/**
 * Adds a layer that paints an icon if resources are missing
 */
fun Layers.addShowLoadingOnMissingResources(styleConfiguration: ShowLoadingIconOnResourceMissingLayer.Style.() -> Unit = {}): ShowLoadingIconOnResourceMissingLayer {
  return ShowLoadingIconOnResourceMissingLayer(styleConfiguration).also {
    addLayer(it)
  }
}
