package com.meistercharts.fx

import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Size
import it.neckar.open.resources.getResourceSafe
import com.meistercharts.resources.JvmLocalResourcePaintableFactory
import it.neckar.open.unit.other.px
import javafx.scene.image.Image

/**
 * Provides JavaFX local resource paintable
 */
class LocalResourcePaintableProviderFX : JvmLocalResourcePaintableFactory {

  override fun get(relativePath: String, size: @px Size?, alignmentPoint: Coordinates): Paintable {
    return javaClass.getResourceSafe("/$relativePath").let {
      val javaFxImage = Image(it.toExternalForm())

      com.meistercharts.canvas.Image(javaFxImage, size ?: javaFxImage.naturalSize, alignmentPoint)
    }
  }
}

private val Image.naturalSize: @px Size
  get() {
    //TODO pixel ratio(?)
    return Size(this.width, this.height)
  }

