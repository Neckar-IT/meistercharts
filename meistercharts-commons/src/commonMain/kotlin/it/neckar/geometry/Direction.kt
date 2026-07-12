/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.geometry

import it.neckar.open.annotations.JsExportForTs
import kotlin.js.JsExport

/**
 * Describes a direction
 *
 * Attention: The order of the direction should *not* matter.
 */
@JsExportForTs
enum class Direction(
  val verticalAlignment: VerticalAlignment,
  val horizontalAlignment: HorizontalAlignment
) {
  Center(VerticalAlignment.Center, HorizontalAlignment.Center),
  CenterLeft(VerticalAlignment.Center, HorizontalAlignment.Left),
  CenterRight(VerticalAlignment.Center, HorizontalAlignment.Right),

  BaseLineCenter(VerticalAlignment.Baseline, HorizontalAlignment.Center),
  BaseLineLeft(VerticalAlignment.Baseline, HorizontalAlignment.Left),
  BaseLineRight(VerticalAlignment.Baseline, HorizontalAlignment.Right),

  TopLeft(VerticalAlignment.Top, HorizontalAlignment.Left),
  TopCenter(VerticalAlignment.Top, HorizontalAlignment.Center),
  TopRight(VerticalAlignment.Top, HorizontalAlignment.Right),

  BottomLeft(VerticalAlignment.Bottom, HorizontalAlignment.Left),
  BottomCenter(VerticalAlignment.Bottom, HorizontalAlignment.Center),
  BottomRight(VerticalAlignment.Bottom, HorizontalAlignment.Right);

  /**
   * Returns the direction with the given vertical alignment
   */
  @JsExport.Ignore
  fun with(verticalAlignment: VerticalAlignment): Direction {
    return get(verticalAlignment, this.horizontalAlignment)
  }

  /**
   * Returns the opposite anchor direction
   */
  @JsExport.Ignore
  fun opposite(): Direction {
    return when (this) {
      Center         -> Center
      CenterLeft     -> CenterRight
      CenterRight    -> CenterLeft
      BaseLineCenter -> BaseLineCenter
      BaseLineLeft   -> BaseLineRight
      BaseLineRight  -> BaseLineLeft
      TopLeft        -> BottomRight
      TopCenter      -> BottomCenter
      TopRight       -> BottomLeft
      BottomLeft     -> TopRight
      BottomCenter   -> TopCenter
      BottomRight    -> TopLeft
    }
  }

  /**
   * Returns the opposite if the given boolean is true
   */
  @JsExport.Ignore
  fun oppositeIf(useOpposite: Boolean): Direction {
    if (useOpposite) {
      return opposite()
    }

    return this
  }

  @JsExport.Ignore
  companion object {
    /**
     * All directions apart from the base-line directions
     */
    val allButBaseline: List<Direction> = listOf(
      Center,
      CenterLeft,
      CenterRight,

      TopLeft,
      TopCenter,
      TopRight,

      BottomLeft,
      BottomCenter,
      BottomRight
    )

    /**
     * Contains the 4 corners
     */
    val corners: List<Direction> = listOf(
      TopLeft,
      TopRight,
      BottomLeft,
      BottomRight
    )

    /**
     * The four sides
     */
    val sides: List<Direction> = listOf(
      CenterLeft,
      TopCenter,
      CenterRight,
      BottomCenter
    )

    /**
     * The four corners and four sides (no center and no base line
     */
    val cornersAndSides: List<Direction> = listOf(
      CenterLeft,
      CenterRight,

      TopLeft,
      TopCenter,
      TopRight,

      BottomLeft,
      BottomCenter,
      BottomRight
    )

    /**
     * Returns the anchor direction for the given vertical and horizontal alignment
     */
    fun get(verticalAlignment: VerticalAlignment, horizontalAlignment: HorizontalAlignment): Direction {
      //do *not* call values() - for performance reasons
      return when (verticalAlignment) {
        VerticalAlignment.Top ->
          when (horizontalAlignment) {
            HorizontalAlignment.Left -> TopLeft
            HorizontalAlignment.Right -> TopRight
            HorizontalAlignment.Center -> TopCenter
          }

        VerticalAlignment.Center ->
          when (horizontalAlignment) {
            HorizontalAlignment.Left -> CenterLeft
            HorizontalAlignment.Right -> CenterRight
            HorizontalAlignment.Center -> Center
          }

        VerticalAlignment.Baseline ->
          when (horizontalAlignment) {
            HorizontalAlignment.Left -> BaseLineLeft
            HorizontalAlignment.Right -> BaseLineRight
            HorizontalAlignment.Center -> BaseLineCenter
          }

        VerticalAlignment.Bottom ->
          when (horizontalAlignment) {
            HorizontalAlignment.Left -> BottomLeft
            HorizontalAlignment.Right -> BottomRight
            HorizontalAlignment.Center -> BottomCenter
          }
      }
    }
  }
}
