package com.meistercharts.algorithms.painter

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Rectangle

/**
 * A Paintable that is able to paint images with inline base64 encoded content.
 * The URI is expected to start with "data:image"
 */
@Suppress("unused")
class ImageDataPaintable(uri: String) : Paintable {
  init {
    require(uri.startsWith(prefix)) { "Invalid uri: <$uri>. Expected to start with $prefix" }
  }

  val delegate: Paintable = UrlPaintable.naturalSize(uri)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return delegate.boundingBox(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    delegate.paint(paintingContext, x, y)
  }

  companion object {
    const val prefix: String = "data:image"
  }
}

