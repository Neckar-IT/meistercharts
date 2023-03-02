package com.meistercharts.painter

import com.meistercharts.algorithms.layers.linechart.PointStyle
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.SeriesIndex
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.style.Palette.getChartColor

/**
 * Paints points associated with a category
 */
fun interface CategoryPointPainter {
  fun paintPoint(
    gc: CanvasRenderingContext,
    x: @Window Double,
    y: @Window Double,
    categoryIndex: CategoryIndex,
    seriesIndex: SeriesIndex,
    value: @Domain Double
  )
}

/**
 * A [CategoryPointPainter] that delegates calls to the given [delegate]
 */
class DelegatingCategoryPointPainter(val delegate: PointPainter) : CategoryPointPainter {
  override fun paintPoint(gc: CanvasRenderingContext, x: Double, y: Double, categoryIndex: CategoryIndex, seriesIndex: SeriesIndex, value: @Domain Double) {
    delegate.paintPoint(gc, x, y)
  }
}

/**
 * Paints no category point at all
 */
val emptyCategoryPointPainter: CategoryPointPainter = DelegatingCategoryPointPainter(NoPointPainter)

/**
 * Uses a [CirclePointPainter] to paint category points
 */
class CircleCategoryPointPainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : CategoryPointPainter {
  val circlePointPainter: CirclePointPainter = CirclePointPainter(snapXValues = snapXValues, snapYValues = snapYValues).apply {
    fill = getChartColor(13)
    stroke = getChartColor(14)
    lineWidth = 2.0
    pointSize = 15.0
  }

  override fun paintPoint(gc: CanvasRenderingContext, x: Double, y: Double, categoryIndex: CategoryIndex, seriesIndex: SeriesIndex, value: @Domain Double) {
    circlePointPainter.paintPoint(gc, x, y)
  }
}

/**
 * Uses a [PointStylePainter] with dot point style
 */
class DotCategoryPointPainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : CategoryPointPainter {
  val pointStylePainter: PointStylePainter = PointStylePainter(PointStyle.Dot, snapXValues = snapXValues, snapYValues = snapYValues)
  override fun paintPoint(gc: CanvasRenderingContext, x: Double, y: Double, categoryIndex: CategoryIndex, seriesIndex: SeriesIndex, value: @Domain Double) {
    pointStylePainter.paintPoint(gc, x, y)
  }
}

/**
 * Uses a [PointStylePainter] with cross point style
 */
class CrossCategoryPointPainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : CategoryPointPainter {
  val pointStylePainter: PointStylePainter = PointStylePainter(PointStyle.Cross, snapXValues = snapXValues, snapYValues = snapYValues)
  override fun paintPoint(gc: CanvasRenderingContext, x: Double, y: Double, categoryIndex: CategoryIndex, seriesIndex: SeriesIndex, value: @Domain Double) {
    pointStylePainter.paintPoint(gc, x, y)
  }
}

/**
 * Uses a [PointStylePainter] with 45° cross point style
 */
class Cross45DegreesCategoryPointPainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : CategoryPointPainter {
  val pointStylePainter: PointStylePainter = PointStylePainter(PointStyle.Cross45Degrees, snapXValues = snapXValues, snapYValues = snapYValues)
  override fun paintPoint(gc: CanvasRenderingContext, x: Double, y: Double, categoryIndex: CategoryIndex, seriesIndex: SeriesIndex, value: @Domain Double) {
    pointStylePainter.paintPoint(gc, x, y)
  }
}
