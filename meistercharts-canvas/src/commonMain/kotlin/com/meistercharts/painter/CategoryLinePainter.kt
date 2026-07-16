/*
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
package com.meistercharts.painter

import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.SeriesIndex
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import it.neckar.open.annotations.Hot

/**
 * Paints lines associated with a category
 */
interface CategoryLinePainter {
  /**
   * Begins a new line
   */
  @Hot
  fun begin(gc: CanvasRenderingContext)

  /**
   * Adds a coordinate to the line.
   *
   * Call [finish] when done.
   */
  @Hot
  fun addCoordinate(
    gc: CanvasRenderingContext,
    x: @Window Double,
    y: @Window Double,
    categoryIndex: CategoryIndex,
    seriesIndex: SeriesIndex,
    value: @Domain Double
  )

  /**
   * Finishes the line created by calls to [addCoordinate]
   */
  @Hot
  fun finish(gc: CanvasRenderingContext)
}

/**
 * A [CategoryLinePainter] that delegates calls to the given [delegate]
 */
open class DelegatingCategoryLinePainter(
  val delegate: LinePainter
) : CategoryLinePainter {
  @Hot
  override fun begin(gc: CanvasRenderingContext) {
    delegate.begin(gc)
  }

  @Hot
  override fun addCoordinate(gc: CanvasRenderingContext, x: Double, y: Double, categoryIndex: CategoryIndex, seriesIndex: SeriesIndex, value: Double) {
    delegate.addCoordinates(gc, x, y)
  }

  @Hot
  override fun finish(gc: CanvasRenderingContext) {
    delegate.paint(gc)
  }
}

/**
 * A [CategoryLinePainter] that paints no lines
 */
val emptyCategoryLinePainter: DelegatingCategoryLinePainter = DelegatingCategoryLinePainter(NoLinePainter)

/**
 * A [CategoryLinePainter] that uses the [DirectLinePainter]
 */
class XyCategoryLinePainter(
  snapXValues: Boolean,
  snapYValues: Boolean,
  linePainterDelegate: LinePainter = DirectLinePainter(snapXValues, snapYValues)
) : DelegatingCategoryLinePainter(linePainterDelegate)
