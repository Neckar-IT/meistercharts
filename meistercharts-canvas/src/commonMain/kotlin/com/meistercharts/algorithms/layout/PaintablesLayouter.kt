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
package com.meistercharts.algorithms.layout

import com.meistercharts.model.Orientation
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.calculateOffsetXWithAnchor
import com.meistercharts.canvas.calculateOffsetYWithAnchor
import com.meistercharts.canvas.layout.cache.CoordinatesCache
import com.meistercharts.canvas.layout.cache.ObjectsCache
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.model.VerticalAlignment
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.fastForEachIndexed
import it.neckar.open.unit.other.Relative
import it.neckar.open.unit.other.px

/**
 * Lays out paintables relative one to another.
 *
 * It is necessary to call [calculate] to precalculate the layout (usually in the layout method of each layer).
 *
 *
 * NOTE: This class does *not* extend Paintable. This is a conscious decision to keep this (already complex) class as simple as possible.
 */
class PaintablesLayouter(
  val configuration: Configuration = Configuration(),
  additionalConfiguration: Configuration.() -> Unit = {},
) {

  init {
    configuration.also(additionalConfiguration)
  }

  /**
   * Contains the calculated variables for each paintable
   */
  private val paintingVariables = object {
    /**
     * The "global" offset for this paintables - depending on the anchoring
     */
    var offsetX: Double = 0.0
    var offsetY = 0.0

    /**
     * Stores the upper left corner of each [Paintable] previously laid out
     */
    val locationsTopLeft = CoordinatesCache()

    /**
     * Contains the bounding boxes for each paintable.
     * The bounding boxes do *not* contain any translation from the layout process.
     */
    val boundingBoxes = ObjectsCache(Rectangle.zero)

    /**
     * The total size over *all* paintables
     */
    var totalSize: Size = Size.zero

    fun calculate(
      paintingContext: LayerPaintingContext,
      /**
       * The (current) paintables provider
       */
      paintables: SizedProvider<Paintable>,

      anchorDirection: Direction = Direction.TopLeft,
      horizontalGap: @px Double = 0.0,
      verticalGap: @px Double = 0.0,

      ) {
      val gc = paintingContext.gc

      locationsTopLeft.ensureSize(paintables.size())
      boundingBoxes.ensureSize(paintables.size())

      //collect the bounding boxes
      paintables.fastForEachIndexed { index, paintable ->
        boundingBoxes[index] = paintable.boundingBox(paintingContext)
      }

      //Calculate the bounding box
      totalSize = calculateTotalSize()

      //Calculate the "global offset"
      offsetX = gc.calculateOffsetXWithAnchor(totalSize.width, horizontalGap, anchorDirection.horizontalAlignment)
      offsetY = gc.calculateOffsetYWithAnchor(totalSize.height, verticalGap, anchorDirection.verticalAlignment)

      //layout all paintables
      layoutPaintables()
    }

    /**
     * Calculates the total bounding box for all paintables.
     *
     * The bounding box has (depending on the [Configuration.layoutOrientation]):
     * - one of width/height: Sum over all paintables
     * - other of width/height: Max of all paintables
     */
    private fun calculateTotalSize(): @px Size {
      return when (configuration.layoutOrientation) {
        Orientation.Vertical -> calculateSizeVerticalLayout()
        Orientation.Horizontal -> calculateSizeHorizontalLayout()
      }
    }

    /**
     * Calculates the size when layouted horizontally
     */
    private fun calculateSizeHorizontalLayout(): @px Size {
      //Does *not* contain the gaps
      @px var netWidth = 0.0
      @px var height = 0.0

      boundingBoxes.fastForEach { boundingBox ->
        netWidth += boundingBox.getWidth()
        height = height.coerceAtLeast(boundingBox.getHeight())
      }

      @px val gapsBetween = calculateGapsBetweenSize()
      return Size.of(netWidth + gapsBetween, height)
    }

    /**
     * Calculates the size when aligned vertically
     */
    private fun calculateSizeVerticalLayout(): @px Size {
      @px var width = 0.0

      /**
       * Without the gaps
       */
      @px var netHeight = 0.0

      boundingBoxes.fastForEach { boundingBox ->
        width = width.coerceAtLeast(boundingBox.getWidth())
        netHeight += boundingBox.getHeight()
      }

      @px val gapsBetween = calculateGapsBetweenSize()
      return Size.of(width, netHeight + gapsBetween)
    }

    /**
     * Calculates the total size of all gaps *between* the paintables
     */
    private fun calculateGapsBetweenSize(): @px Double {
      return configuration.gap * (boundingBoxes.size - 1)
    }

    /**
     * Layout the paintables
     */
    private fun layoutPaintables() {
      when (configuration.layoutOrientation) {
        Orientation.Horizontal -> layoutHorizontally()
        Orientation.Vertical -> layoutVertically()
      }
    }

    /**
     * Layout the paintables horizontally
     */
    private fun layoutHorizontally() {
      val alignment: VerticalAlignment = configuration.verticalAlignment //other direction!

      @px val height = totalSize.height
      @px val halfHeight = height / 2.0


      //Start at the origin, layout from left to right
      @px var currentX = 0.0

      boundingBoxes.fastForEachWithIndex { index, boundingBoxForPaintable ->
        val y = when (alignment) {
          VerticalAlignment.Top -> 0.0
          VerticalAlignment.Center, VerticalAlignment.Baseline -> 0.0 + halfHeight - boundingBoxForPaintable.getHeight() / 2.0
          VerticalAlignment.Bottom -> 0.0 + height - boundingBoxForPaintable.getHeight()
        }

        //save the current location for this paintable
        locationsTopLeft.set(index, currentX, y)

        currentX += boundingBoxForPaintable.getWidth() + configuration.gap
      }
    }

    /**
     * Layout the paintables vertically
     */
    private fun layoutVertically() {
      val alignment = configuration.horizontalAlignment //other direction!

      @px val width = totalSize.width
      @px val halfWidth = width / 2.0

      //Start at the origin, layout from top to bottom
      @px var currentY = 0.0

      boundingBoxes.fastForEachWithIndex { index, boundingBoxForPaintable ->
        val x = when (alignment) {
          HorizontalAlignment.Left -> 0.0
          HorizontalAlignment.Right -> width - boundingBoxForPaintable.getWidth()
          HorizontalAlignment.Center -> halfWidth - boundingBoxForPaintable.getWidth() / 2.0
        }

        //save the current location for this paintable
        locationsTopLeft.set(index, x, currentY)

        currentY += boundingBoxForPaintable.getHeight() + configuration.gap
      }
    }
  }

  /**
   * Calculates the layout
   */
  fun calculate(
    paintingContext: LayerPaintingContext,
    paintables: SizedProvider<Paintable>,

    anchorDirection: Direction = Direction.TopLeft,
    horizontalGap: @px Double = 0.0,
    verticalGap: @px Double = 0.0,
  ) {

    paintingVariables.calculate(paintingContext, paintables, anchorDirection, horizontalGap, verticalGap)
  }

  /**
   * Retrieve the bounding box of the paintable (itself).
   * ATTENTION: Does *not* contain location information from the layout
   */
  fun boundingBox(paintableIndex: PaintableIndex): Rectangle {
    return paintingVariables.boundingBoxes[paintableIndex.value]
  }

  fun locationTopLeftX(paintableIndex: PaintableIndex): @Zoomed Double {
    return paintingVariables.locationsTopLeft.x(paintableIndex.value)
  }

  fun locationTopLeftY(paintableIndex: PaintableIndex): @Zoomed Double {
    return paintingVariables.locationsTopLeft.y(paintableIndex.value)
  }

  /**
   * Returns the total size - for all paintables
   */
  fun totalSize(): Size {
    return paintingVariables.totalSize
  }

  /**
   * The offset x position
   */
  fun x(): Double {
    return paintingVariables.offsetX
  }

  /**
   * The offset x position
   */
  fun y(): Double {
    return paintingVariables.offsetY
  }

  /**
   * Paints all paintables
   */
  fun paintAllPaintables(
    paintingContext: LayerPaintingContext,
    paintables: MultiProvider<PaintableIndex, Paintable>,
  ) {
    val gc = paintingContext.gc

    gc.translate(paintingVariables.offsetX, paintingVariables.offsetY)
    if (gc.debug[DebugFeature.ShowAnchors]) {
      gc.paintMark()
    }

    paintingVariables.boundingBoxes.fastForEachWithIndex { paintableIndexAsInt, _ ->
      val paintableIndex = PaintableIndex(paintableIndexAsInt)
      val paintable = paintables.valueAt(paintableIndex)

      gc.saved {
        paintSinglePaintable(paintableIndex, paintable, paintingContext)
      }
    }
  }

  /**
   * Paints the paintable at the precalculated location
   */
  fun paintSinglePaintable(paintableIndex: PaintableIndex, paintable: Paintable, paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.translate(locationTopLeftX(paintableIndex), locationTopLeftY(paintableIndex))

    val boundingBox = boundingBox(paintableIndex)

    if (gc.debug[DebugFeature.ShowBounds]) {
      gc.stroke(Color.orange)
      gc.strokeRect(boundingBox)
    }
    if (gc.debug[DebugFeature.ShowAnchors]) {
      gc.paintMark()
    }

    //Use the size of the paintable (from the bounding box). But use the calculated location
    paintable.paintInBoundingBox(paintingContext, Coordinates.origin, Direction.TopLeft, boundingBox.size)
  }

  /**
   * Returns the paintable index for the given location - relative to the layouter!
   */
  fun findIndex(x: @Zoomed @Relative("layouter origin") Double, y: @Zoomed @Relative("layouter origin") Double): PaintableIndex? {
    val offsetCorrectedX = x - paintingVariables.offsetX
    val offsetCorrectedY = y - paintingVariables.offsetY

    paintingVariables.locationsTopLeft.fastForEachIndexed { paintableIndexAsInt, topLeftX, topLeftY ->
      if (offsetCorrectedX < topLeftX) {
        //requested coords too far to the left
        return@fastForEachIndexed
      }
      if (offsetCorrectedY < topLeftY) {
        //requested coords too far to the top
        return@fastForEachIndexed
      }

      //we know, that we are right and below of topLeft

      //check for bounding boxes
      val paintableBoundingBox = paintingVariables.boundingBoxes[paintableIndexAsInt]

      if (offsetCorrectedX > topLeftX + paintableBoundingBox.getWidth()) {
        //too far to the right
        return@fastForEachIndexed
      }

      if (offsetCorrectedY > topLeftY + paintableBoundingBox.getHeight()) {
        //too far below
        return@fastForEachIndexed
      }

      return PaintableIndex(paintableIndexAsInt)
    }

    return null
  }

  class Configuration {
    /**
     * The orientation in which to lay out the [Paintable]s
     */
    var layoutOrientation: Orientation = Orientation.Horizontal

    /**
     * How the paintables are aligned horizontally.
     * Only relevant if [layoutOrientation] is set to [Orientation.Vertical]
     */
    var horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Center

    /**
     * How the paintables are aligned vertically.
     * Only relevant if [layoutOrientation] is set to [Orientation.Horizontal]
     */
    var verticalAlignment: VerticalAlignment = VerticalAlignment.Center

    /**
     * The gap between each [Paintable]
     */
    var gap: @px Double = 5.0
  }
}
