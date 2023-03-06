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
package com.meistercharts.canvas.paintable

import it.neckar.open.annotations.Internal
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.paintable.ObjectFit
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.calculateOffsetXWithAnchor
import com.meistercharts.canvas.calculateOffsetYWithAnchor
import com.meistercharts.model.Anchoring
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px

/**
 * Provides a paintable for a given layer painting context.
 *
 * The context can be used to resolve the locales or other information
 */
typealias PaintableProvider = (LayerPaintingContext) -> Paintable

/**
 * A paintable paints itself on a canvas. It can be an image, a set of paint instructions or something else.
 *
 * A paintable paints itself relative to the given location.
 * The paintable decides itself in which direction to the given location it is painted.
 *
 * The bounding box of the paintable can be used to layout a paintable (e.g. when aligning paintables in a toolbar or legend)
 * The method [paintInBoundingBox] uses this information to align a paintable.
 *
 * A paintable can paint in all directions - relative to the start location.
 *
 * There are two types methods:
 *
 * * [paint]: The paintable paints itself at the given location.
 * * [paintInBoundingBox]: The paintable is painted at the given location using the anchor - depending on the bounding box. Useful for legend/toolbars
 *
 * ATTENTION: There is also the interface [ResizablePaintable] which allows the user of the [Paintable] to change its size.
 */
interface Paintable {
  /**
   * Returns the bounding box of a paintable.
   * The bounding box contains the *visual* content of a paintable.
   * It is possible that a paintable paints smaller details *outside* the bounding box (e.g. labels)
   *
   *
   * The bounding box can cover any area.
   * The alignment point (this is the point where the paintable is aligned to) may be inside or outside the bounding box.
   * The x/y coordinate of the bounding box represents the "translation" from the alignment point to the upper left corner of the bounding box (that has happened already).
   * Therefore, negative x/y values represent a translation to the upper left or put differently: the alignment point lies to the right and bottom from the upper left corner of the bounding box.
   *
   * # Example with the alignment point inside bounding box
   * ```
   * ----------------------
   * |                    |
   * |                    |
   * |       * (a. point) |
   * |                    |
   * ----------------------
   * ```
   *
   * Suppose that bounding box has a width of 150 px and a height of 75 px.
   * Since the upper left corner of the bounding box is to the left and to the top of the alignment point x/y of the bounding box are negative, e.g. : -60/-50
   *
   * # Example with the alignment point outside bounding box
   * ```
   * ----------------------
   * |                    |
   * |                    |
   * |                    |
   * |                    |
   * ----------------------
   *
   *           * (alignment point)
   * ```
   *
   * Suppose that bounding box has a width of 150 px and a height of 75 px.
   * The upper left corner of the bounding box would be: -75/-100
   *
   * # Alignment Point
   * The x/y coordinates of the bounding box describe where the alignment point is located:
   * * x/y = 0/0: the alignment point coincides with the upper left corner of the bounding box
   * * x/y = -width/-height: the alignment point coincides with the lower right corner of the bounding box
   */
  fun boundingBox(paintingContext: LayerPaintingContext): @Zoomed Rectangle

  /**
   * Paints this [Paintable] using the original width and height of this [Paintable].
   *
   * [x]/[y] is where the alignment point of the paintable should be painted.
   */
  fun paint(
    paintingContext: LayerPaintingContext,
    @Zoomed x: Double = 0.0,
    @Zoomed y: Double = 0.0
  )

  /**
   * Paints this [Paintable] using the original width and height of this [Paintable].
   *
   * [location] is where the alignment point of the paintable should be painted.
   */
  fun paint(paintingContext: LayerPaintingContext, location: Coordinates) {
    this.paint(paintingContext, location.x, location.y)
  }

  /**
   * Paints this [Paintable] - using the given anchor in the given bounding box.
   * Attention: The paintable is scaled to fit into the bounding box
   *
   * @param paintingContext the canvas rendering context
   * @param anchoring the anchor to be used
   */
  fun paintInBoundingBox(
    paintingContext: LayerPaintingContext,
    @Zoomed anchoring: Anchoring,
    /**
     * The size of the bounding box
     */
    boundingBoxSize: @Zoomed Size = boundingBox(paintingContext).size,
    /**
     * The object fit that is used to resize the paintable
     */
    objectFit: ObjectFit = ObjectFit.ContainNoGrow,
  ) {
    paintInBoundingBox(
      paintingContext,
      anchoring.anchor.x,
      anchoring.anchor.y,
      anchoring.anchorDirection,
      anchoring.gapHorizontal,
      anchoring.gapVertical,
      boundingBoxSize.width,
      boundingBoxSize.height,
      objectFit
    )
  }

  /**
   * Paints this [Paintable]
   * @param paintingContext the canvas rendering context
   * @param location where to paint the paintable
   * @param direction the direction in which to find [location]
   *
   * This method does *not* take a gap. If a gap is required call on of the other [paintInBoundingBox] methods.
   */
  fun paintInBoundingBox(
    paintingContext: LayerPaintingContext,
    @Zoomed location: Coordinates,
    direction: Direction,
    /**
     * The size of the bounding box
     */
    boundingBoxSize: @Zoomed Size = boundingBox(paintingContext).size,
    /**
     * The object fit that is used to resize the paintable
     */
    objectFit: ObjectFit = ObjectFit.ContainNoGrow,
  ) {
    paintInBoundingBox(
      paintingContext = paintingContext,
      x = location.x,
      y = location.y,
      anchorDirection = direction,
      gapHorizontal = 0.0,
      gapVertical = 0.0,
      width = boundingBoxSize.width,
      height = boundingBoxSize.height,
      objectFit = objectFit
    )
  }

  fun paintInBoundingBox(
    paintingContext: LayerPaintingContext,
    @Zoomed boundingBox: Rectangle,
    direction: Direction,
    /**
     * The object fit that is used to resize the paintable
     */
    objectFit: ObjectFit = ObjectFit.ContainNoGrow,
  ) {
    paintInBoundingBox(
      paintingContext,
      boundingBox.location,
      direction,
      boundingBox.size,
      objectFit
    )
  }

  /**
   * Paints this [Paintable]
   * @param paintingContext the canvas rendering context
   * @param x where to paint the paintable
   * @param y where to paint the paintable
   * @param direction the direction in which to paint relative to the given x/y coordinates
   */
  fun paintInBoundingBox(
    paintingContext: LayerPaintingContext,
    @Zoomed x: Double = 0.0,
    @Zoomed y: Double = 0.0,
    direction: Direction,
    gapHorizontal: Double = 0.0,
    gapVertical: Double = gapHorizontal,
    /**
     * The size of the bounding box
     */
    boundingBoxSize: @Zoomed Size = boundingBox(paintingContext).size,
    /**
     * The object fit that is used to resize the paintable
     */
    objectFit: ObjectFit = ObjectFit.ContainNoGrow,
  ) {
    paintInBoundingBox(paintingContext, x, y, direction, gapHorizontal, gapVertical, boundingBoxSize.width, boundingBoxSize.height, objectFit)
  }

  /**
   * Paints this [Paintable] into the given bounding box
   * @param paintingContext the canvas rendering context
   * @param x the x-coordinate of the location where to paint the paintable
   * @param y the y-coordinate of the location where to paint the paintable
   * @param anchorDirection the direction in which to find [x]/[y]
   * @param gap the distance between [x]/[y] and the anchor
   */
  fun paintInBoundingBox(
    paintingContext: LayerPaintingContext,
    x: @Zoomed Double = 0.0,
    y: @Zoomed Double = 0.0,
    anchorDirection: Direction,
    gapHorizontal: @Zoomed Double = 0.0,
    gapVertical: @Zoomed Double = gapHorizontal,
    /**
     * The width of the bounding box
     */
    width: @Zoomed Double,
    /**
     * The height of the bounding box
     */
    height: @Zoomed Double,
    /**
     * The object fit that is used to resize the paintable
     */
    objectFit: ObjectFit = ObjectFit.ContainNoGrow
  ) {
    //TODO add scaling

    //The alignment point where the paintable is painted from
    val alignmentX = x + paintingContext.gc.calculateOffsetXWithAnchor(width, gapHorizontal, anchorDirection.horizontalAlignment)
    val alignmentY = y + anchorDirection.verticalAlignment.calculateOffsetYWithAnchor(height, gapVertical, paintingContext.gc)

    @px val currentBoundingBox = boundingBox(paintingContext)
    @px val size = currentBoundingBox.size

    when (objectFit) {
      ObjectFit.None -> {
        //No resizing at all
        val paintableAlignment = currentBoundingBox.location
        paint(paintingContext, alignmentX - paintableAlignment.x, alignmentY - paintableAlignment.y)
      }

      ObjectFit.ContainNoGrow -> {
        //The target size
        val targetSize = size.withMax(width, height).fitWithAspectRatio(size.aspectRatio)

        //Keep aspect ratio
        val scaleX = 1.0 / size.width * targetSize.width
        val scaleY = 1.0 / size.height * targetSize.height

        @px val deltaX = size.width * scaleX - width
        @px val deltaY = size.height * scaleY - height

        paintSizeForced(paintingContext, alignmentX - deltaX / 2.0, alignmentY - deltaY / 2.0, targetSize)
      }

      ObjectFit.Contain -> {
        //The target size
        val targetSize = Size(width, height).fitWithAspectRatio(size.aspectRatio)

        //Keep aspect ratio
        val scaleX = 1.0 / size.width * targetSize.width
        val scaleY = 1.0 / size.height * targetSize.height

        @px val deltaX = size.width * scaleX - width
        @px val deltaY = size.height * scaleY - height

        paintSizeForced(paintingContext, alignmentX - deltaX / 2.0, alignmentY - deltaY / 2.0, targetSize)
      }

      ObjectFit.Fill -> {
        paintSizeForced(paintingContext, alignmentX, alignmentY, Size(width, height))
      }
    }
  }

  /**
   * Internal method: Do *not* call this method directly.
   * Instead use one of the [paintInBoundingBox] methods.
   *
   * Paints the paintable within the given bounding box - the paintable must place itself exactly into this bounding box.
   * The paintable is *forced* to use the provided size.
   *
   * The paintable has to resize itself accordingly.
   * The default implementation uses [CanvasRenderingContext#scale] to scale the paintable.
   *
   * This method can be used if a paintable is not used as is, but instead it is placed in a bounding box
   */
  @Internal("Use paintInBoundingBox instead")
  fun paintSizeForced(
    /**
     * The painting context
     */
    paintingContext: LayerPaintingContext,
    /**
     * The x location of the bounding box (upper left corner)
     */
    x: @px Double,
    /**
     * The y location of the bounding box (upper left corner)
     */
    y: @px Double,
    /**
     * The forced size. Does *not* correspond to the own size of the paintable ([size])
     */
    forcedSize: @Zoomed Size
  ) {
    @px val currentBoundingBox = boundingBox(paintingContext)
    @px val size = currentBoundingBox.size

    val scaleX = 1.0 / size.width * forcedSize.width
    val scaleY = 1.0 / size.height * forcedSize.height

    val gc = paintingContext.gc

    gc.translate(x, y)
    gc.scale(scaleX, scaleY)

    //Add the bounding box location
    val location = boundingBox(paintingContext).location
    paint(paintingContext, -location.x, -location.y)
  }

  companion object {
    /**
     * Noop paintable - has an empty size and does nothing
     */
    val NoOp: Paintable = object : Paintable {
      override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle = Rectangle.zero

      override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
      }
    }
  }
}

