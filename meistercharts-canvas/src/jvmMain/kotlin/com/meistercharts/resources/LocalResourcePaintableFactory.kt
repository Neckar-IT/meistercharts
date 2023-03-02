package com.meistercharts.resources

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.paintable.ObjectFit
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Anchoring
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px


/**
 * Loads a local resource
 */
actual class LocalResourcePaintable @JvmOverloads actual constructor(
  val relativePath: String,
  size: @px Size?,
  val alignmentPoint: Coordinates
) : Paintable {

  val delegate: Paintable = jvmLocalResourcePaintableFactory.get(relativePath, size, alignmentPoint)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return delegate.boundingBox(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    delegate.paint(paintingContext, x, y)
  }

  override fun paint(paintingContext: LayerPaintingContext, location: Coordinates) {
    delegate.paint(paintingContext, location)
  }

  override fun paintInBoundingBox(paintingContext: LayerPaintingContext, anchoring: Anchoring, boundingBoxSize: Size, objectFit: ObjectFit) {
    delegate.paintInBoundingBox(paintingContext, anchoring, boundingBoxSize, objectFit)
  }

  override fun paintInBoundingBox(paintingContext: LayerPaintingContext, location: Coordinates, direction: Direction, boundingBoxSize: Size, objectFit: ObjectFit) {
    delegate.paintInBoundingBox(paintingContext, location, direction, boundingBoxSize, objectFit)
  }

  override fun paintInBoundingBox(
    paintingContext: LayerPaintingContext, x: Double, y: Double, direction: Direction,
    gapHorizontal: Double, gapVertical: Double,
    boundingBoxSize: Size, objectFit: ObjectFit
  ) {
    delegate.paintInBoundingBox(paintingContext, x, y, direction, gapHorizontal, gapVertical, boundingBoxSize, objectFit)
  }

  override fun paintInBoundingBox(
    paintingContext: LayerPaintingContext, x: Double, y: Double, anchorDirection: Direction,
    gapHorizontal: Double, gapVertical: Double, width: Double, height: Double, objectFit: ObjectFit
  ) {
    delegate.paintInBoundingBox(paintingContext, x, y, anchorDirection, gapHorizontal, gapVertical, width, height, objectFit)
  }

  override fun paintSizeForced(paintingContext: LayerPaintingContext, x: Double, y: Double, forcedSize: Size) {
    delegate.paintSizeForced(paintingContext, x, y, forcedSize)
  }

  actual fun withSize(size: Size): LocalResourcePaintable {
    return LocalResourcePaintable(relativePath, size, alignmentPoint)
  }

  actual companion object {
  }
}

/**
 * Holds the current instance of the local resource paintable provider
 */
var jvmLocalResourcePaintableFactory: JvmLocalResourcePaintableFactory = JvmLocalResourcePaintableFactory { _, _, _ ->
  throw UnsupportedOperationException("please set the jvmLocalResourcePaintableFactory for the current platform by calling MeisterChartPlatform.init()")
}


/**
 * Provides jvm local resource paintables.
 */
fun interface JvmLocalResourcePaintableFactory {
  /**
   * Returns the paintable
   */
  fun get(relativePath: String, size: @px Size?, alignmentPoint: Coordinates): Paintable
}
