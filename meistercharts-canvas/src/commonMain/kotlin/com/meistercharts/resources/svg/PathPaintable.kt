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
package com.meistercharts.resources.svg

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.color.Color
import com.meistercharts.algorithms.painter.Path
import com.meistercharts.algorithms.painter.PathActions
import com.meistercharts.canvas.calculateOffsetXWithAnchor
import com.meistercharts.canvas.calculateOffsetYWithAnchor
import com.meistercharts.canvas.paintable.Paintable
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import com.meistercharts.style.Palette
import it.neckar.open.unit.other.px
import kotlin.math.min

/**
 * Converts a path to a [PathPaintableProvider]
 */
fun Path.toProvider(
  /**
   * The size of the path itself
   */
  pathSize: Size,

  /**
   * The default fill
   */
  defaultFill: Color = Palette.defaultGray
): PathPaintableProvider {
  return PathPaintableProvider(this, pathSize, defaultFill)
}

/**
 * A paintable provider that provides paintables that are based upon a path
 */
class PathPaintableProvider(
  val path: Path,
  val pathSize: Size,
  /**
   * The default fill that is used if no fill is provided in the [get] method
   */
  val defaultFill: Color?,

  /**
   * The default stroke that is used if no fill is provided in the [get] method
   */
  val defaultStroke: Color? = null,
) {
  /**
   * Returns the paintable for the given size and with the given fill
   */
  fun get(targetSize: Size, fill: Color? = defaultFill, stroke: Color? = defaultStroke, strokeWidth: @px Double = 1.0, alignmentPoint: Coordinates = Coordinates.origin): PathPaintable {
    return PathPaintable.create(path, pathSize, targetSize, fill, stroke, strokeWidth, alignmentPoint)
  }

  /**
   * Returns the paintable for the given size and with the given fill. Aligns the paintable at the given alignment
   */
  fun get(targetSize: Size, fill: Color? = defaultFill, stroke: Color? = defaultStroke, alignment: Direction): PathPaintable {
    val x = alignment.horizontalAlignment.calculateOffsetXWithAnchor(targetSize.width, 0.0)
    val y = alignment.verticalAlignment.calculateOffsetYWithAnchor(targetSize.height, 0.0, null)

    return get(targetSize, fill, stroke, alignmentPoint = Coordinates.of(x, y))
  }
}

/**
 * A paintable that paints a path
 */
class PathPaintable(
  /**
   * The path actions
   */
  val pathActions: PathActions,
  /**
   * The size of the path - is used in the bounding box
   */
  val size: Size,
  /**
   * The scale factor (x and y) that is used when painting the path
   */
  val scale: Double,
  /**
   * The offset for the path. The offset is set if the path has not the same size as [size].
   */
  val offset: Distance,
  /**
   * The fill for the path paintable.
   * If [fill] is null, the paintable does call fill at all
   */
  val fill: Color?,

  /**
   * The stroke for the path paintable
   * If [stroke] is nul, the paintable does not call stroke at all
   */
  val stroke: Color?,

  /**
   * The line width for the stroke
   */
  val strokeWidth: @px Double = 1.0,

  /**
   * The alignment point for the path
   */
  val alignmentPoint: Coordinates = Coordinates.origin,
) : Paintable {

  val boundingBox: Rectangle = Rectangle(alignmentPoint, size)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return boundingBox
  }

  @Suppress("ReplaceManualRangeWithIndicesCalls") //avoid instantiation of iterable
  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    gc.translate(x + offset.x + alignmentPoint.x, y + offset.y + alignmentPoint.y)

    gc.beginPath()

    val actions = pathActions.actions
    for (i in 0 until actions.size) {
      gc.pathAction(actions[i], scale, scale)
    }

    fill?.let {
      gc.fill(it)
      gc.fill()
    }
    stroke?.let {

      strokeWidth?.let {
        gc.lineWidth = it
      }

      gc.stroke(it)
      gc.stroke()
    }
  }

  /**
   * Returns a new path paintable with modified fill
   */
  fun withFill(fill: Color): PathPaintable {
    return PathPaintable(pathActions, size, scale, offset, fill, stroke, strokeWidth, alignmentPoint)
  }

  fun withStroke(stroke: Color): PathPaintable {
    return PathPaintable(pathActions, size, scale, offset, fill, stroke, strokeWidth, alignmentPoint)
  }

  companion object {
    /**
     * Creates a resized paintable based upon the given path
     */
    fun create(
      /**
       * The path
       */
      path: Path,
      /**
       * The size of the path
       */
      pathSize: Size,
      /**
       * The target size of the paintable
       */
      targetSize: Size,
      /**
       * The fill color
       */
      fill: Color?,
      /**
       * The stroke color - if there is any
       */
      stroke: Color?,
      /**
       * The stroke width
       */
      strokeWidth: @px Double = 1.0,
      /**
       * The alignment point
       */
      alignmentPoint: Coordinates = Coordinates.origin,
    ): PathPaintable {
      check(pathSize.width == pathSize.height) { "width must be equal to height but was ${pathSize.width} != ${pathSize.height}" }

      val factorWidth = targetSize.width.div(pathSize.width)
      val factorHeight = targetSize.height.div(pathSize.height)

      //the factor that is used for both sides
      val factor = min(factorWidth, factorHeight)

      //calculate the offset - to place the path in the center
      val netWidth = pathSize.width * factor
      val netHeight = pathSize.height * factor

      val deltaX = targetSize.width - netWidth
      val deltaY = targetSize.height - netHeight
      val offset: Distance = Distance(deltaX / 2.0, deltaY / 2.0)

      return PathPaintable(path, targetSize, factor, offset, fill, stroke, strokeWidth, alignmentPoint)
    }
  }
}
