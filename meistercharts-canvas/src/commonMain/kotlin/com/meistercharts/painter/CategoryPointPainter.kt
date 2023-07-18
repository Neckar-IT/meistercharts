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
package com.meistercharts.painter

import com.meistercharts.algorithms.layers.linechart.PointStyle
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.SeriesIndex
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
