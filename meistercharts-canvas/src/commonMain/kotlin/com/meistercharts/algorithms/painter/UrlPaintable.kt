/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.algorithms.painter

import com.meistercharts.environment
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.Image
import com.meistercharts.canvas.loadImage
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.geometry.Coordinates
import com.meistercharts.geometry.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px

/**
 * Draws an image denoted by an absolute url.
 */
class UrlPaintable
/**
 * Use the methods in the companion object to create new instances
 */
private constructor(
  /**
   * The URL
   */
  val url: String,

  /**
   * The size of the bounding box.
   * If set to null, the natural size of the loaded image is used.
   */
  val sizeOverride: @px Size? = null,

  /**
   * The alignment point of the bounding box
   */
  val alignmentPoint: Coordinates = Coordinates.origin
) : Paintable {

  private var boundingBox = recalculateBoundingBox()

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return boundingBox
  }

  /**
   * Recalculates the bounding box
   */
  private fun recalculateBoundingBox(): Rectangle {
    return Rectangle(alignmentPoint, sizeOverride ?: naturalSizeAdjusted ?: Size.none)
  }

  /**
   * Returns the natural size of the image behind the url.
   * Returns null if the image has not yet been loaded
   */
  val naturalSize: @PhysicalPixel Size?
    get() {
      return image?.size
    }

  /**
   * Returns the natural size of the image behind the url adjusted to the device-pixel ratio.
   * Returns null if the image has not yet been loaded
   */
  val naturalSizeAdjusted: @Zoomed Size?
    get() {
      return naturalSize?.divide(environment.devicePixelRatio)
    }

  /**
   * Returns true if the image has been loaded
   */
  val imageLoaded: Boolean
    get() = image != null

  /**
   * Contains the image - returns null until the image has been resolved
   */
  var image: Image? = null
    private set

  init {
    loadImage(url) {
      this.image = it
      //Recalculate the bounding box after the image has been loaded - necessary to update the natural size
      boundingBox = recalculateBoundingBox()
    }
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    image.let { image ->
      if (image != null) {
        if (sizeOverride == null) {
          //Paint in the natural size - pixel perfect
          paintingContext.gc.paintImagePixelPerfect(image, x + alignmentPoint.x, y + alignmentPoint.y)
        } else {
          //Paint with the given size - *not* pixel perfect
          paintingContext.gc.paintImage(image, x + alignmentPoint.x, y + alignmentPoint.y, sizeOverride.width, sizeOverride.height)
        }
      } else {
        paintingContext.missingResources.reportMissing(this)
      }
    }
  }

  /**
   * Is overridden because paintImagePixelPerfect does *not* respect the scale
   */
  override fun paintSizeForced(paintingContext: LayerPaintingContext, x: Double, y: Double, forcedSize: Size) {
    image.let { image ->
      if (image != null) {
        paintingContext.gc.paintImage(image, x, y, forcedSize.width, forcedSize.height)
      } else {
        paintingContext.missingResources.reportMissing(this)
      }
    }
  }

  /**
   * Returns a new url paintable with a new size.
   * Does *not* reload the image. Only changes the size.
   */
  fun withSize(size: Size): UrlPaintable {
    return UrlPaintable(url, size, alignmentPoint).also {
      it.image = image
    }
  }

  override fun toString(): String {
    return "UrlPaintable(url='$url', sizeOverride=$sizeOverride, imageLoaded=$imageLoaded)"
  }

  companion object {
    /**
     * Creates an url paintable that uses the natural size of the loaded image
     */
    fun naturalSize(
      /**
       * The URL
       */
      url: String,
      /**
       * The alignment point for the bounding box
       */
      alignmentPoint: Coordinates = Coordinates.origin
    ): UrlPaintable {
      return UrlPaintable(url, null, alignmentPoint)
    }

    /**
     * Creates an url paintable with the given size.
     * Does *not* respect the natural aspect ratio of the loaded image.
     */
    fun fixedSize(
      /**
       * The URL
       */
      url: String,
      /**
       * The size of the paintable
       */
      size: @px Size,
      /**
       * The alignment point for the bounding box
       */
      alignmentPoint: Coordinates = Coordinates.origin
    ): UrlPaintable {
      return UrlPaintable(url, size, alignmentPoint)
    }

    fun fixedSizeCentered(
      /**
       * The URL
       */
      url: String,
      /**
       * The size of the paintable
       */
      size: @px Size
    ): UrlPaintable {
      return UrlPaintable(url, size, Coordinates(-size.width / 2.0, -size.height / 2.0))
    }
  }
}
