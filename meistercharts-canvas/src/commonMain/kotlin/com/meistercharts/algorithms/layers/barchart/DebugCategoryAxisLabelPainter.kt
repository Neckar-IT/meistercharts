package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.strokeRect
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import com.meistercharts.provider.SizedLabelsProvider

/**
 * Paints debug info
 */
class DebugCategoryAxisLabelPainter : CategoryAxisLabelPainter {
  override fun layout(categoryLayout: EquisizedBoxLayout, labelsProvider: SizedLabelsProvider, labelVisibleCondition: LabelVisibleCondition) {
    //nothing to layout
  }

  override fun paint(
    paintingContext: LayerPaintingContext,
    x: @Window Double,
    y: @Window Double,
    width: @Zoomed Double,
    height: @Zoomed Double,
    tickDirection: Direction,
    label: String?,
    categoryIndex: CategoryIndex,
    categoryAxisOrientation: Orientation,
  ) {
    paintingContext.gc.apply {
      stroke(Color.green)
      lineWidth = 1.0
      strokeRect(x, y, width, height, tickDirection)
      paintMark(x, y, color = Color.orange)
    }
  }
}
