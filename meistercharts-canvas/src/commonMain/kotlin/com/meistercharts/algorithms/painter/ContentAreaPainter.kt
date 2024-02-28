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
package com.meistercharts.algorithms.painter

import com.meistercharts.annotations.Window
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.color.ColorProviderNullable
import com.meistercharts.model.SidesSelection

/**
 * Painter that draws the valid area (x and y axis)
 *
 */
class ContentAreaPainter {
  var stroke: ColorProviderNullable = null
  var sidesToPaint: SidesSelection = SidesSelection.all

  private val areaPainter: RectangleAreaPainter = RectangleAreaPainter(snapXValues = false, snapYValues = false).apply {
    fill = null
  }

  fun paint(
    gc: CanvasRenderingContext,
    chartCalculator: ChartCalculator
  ) {

    areaPainter.borderSides = sidesToPaint
    areaPainter.borderColor = stroke

    @Window val fromX = chartCalculator.contentAreaRelative2windowX(0.0)
    @Window val toX = chartCalculator.contentAreaRelative2windowX(1.0)
    @Window val fromY = chartCalculator.contentAreaRelative2windowY(0.0)
    @Window val toY = chartCalculator.contentAreaRelative2windowY(1.0)
    areaPainter.paintArea(gc, fromX, fromY, toX, toY)
  }

}
