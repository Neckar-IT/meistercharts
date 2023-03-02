package com.meistercharts.painter

import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.SeriesIndex
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext

/**
 * Paints lines associated with a category
 */
interface CategoryLinePainter {
  /**
   * Begins a new line
   */
  fun begin(gc: CanvasRenderingContext)

  /**
   * Adds a coordinate to the line.
   *
   * Call [finish] when done.
   */
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
  fun finish(gc: CanvasRenderingContext)
}

/**
 * A [CategoryLinePainter] that delegates calls to the given [delegate]
 */
open class DelegatingCategoryLinePainter(
  val delegate: LinePainter
) : CategoryLinePainter {
  override fun begin(gc: CanvasRenderingContext) {
    delegate.begin(gc)
  }

  override fun addCoordinate(gc: CanvasRenderingContext, x: Double, y: Double, categoryIndex: CategoryIndex, seriesIndex: SeriesIndex, value: Double) {
    delegate.addCoordinate(gc, x, y)
  }

  override fun finish(gc: CanvasRenderingContext) {
    delegate.finish(gc)
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
