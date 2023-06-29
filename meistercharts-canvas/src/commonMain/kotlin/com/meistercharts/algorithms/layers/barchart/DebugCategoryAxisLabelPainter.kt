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
package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.color.Color
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
