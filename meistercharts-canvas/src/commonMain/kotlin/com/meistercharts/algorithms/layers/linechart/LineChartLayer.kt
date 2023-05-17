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
package com.meistercharts.algorithms.layers.linechart

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.painter.LinePainter
import com.meistercharts.painter.PointPainter
import com.meistercharts.painter.PointStylePainter
import com.meistercharts.style.Palette
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProviderIndexContextAnnotation

/**
 * A layer that displays lines
 */
open class LineChartLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    data.model.linesCount.fastFor { lineIndex ->
      paintLine(paintingContext, lineIndex)
    }
  }

  /**
   * Paints a single line - including the points
   */
  private fun paintLine(paintingContext: LayerPaintingContext, lineIndex: Int) {
    //Skip all empty lines
    val dataPointCount = data.model.pointsCount(lineIndex)
    if (dataPointCount <= 0) {
      return
    }

    val chartCalculator = paintingContext.chartCalculator
    val gc = paintingContext.gc

    style.lineStyles.valueAt(lineIndex).apply(gc)

    val linePainter = style.linePainters.valueAt(lineIndex)

    linePainter.begin(gc)
    dataPointCount.fastFor { pointIndex ->
      @Window val x = chartCalculator.domainRelative2windowX(data.model.valueX(lineIndex, pointIndex))
      @Window val y = chartCalculator.domainRelative2windowY(data.model.valueY(lineIndex, pointIndex))
      linePainter.addCoordinates(gc, x, y)
    }

    linePainter.paint(gc)

    //Paint the points - must be done after the line has finished to be ensure the z order is correct
    paintPoints(paintingContext, lineIndex, dataPointCount)
  }

  protected fun paintPoints(paintingContext: LayerPaintingContext, lineIndex: Int, dataPointCount: Int) {
    val chartCalculator = paintingContext.chartCalculator
    val gc = paintingContext.gc

    val pointPainter = style.pointPainters.valueAt(lineIndex)

    dataPointCount.fastFor { pointIndex ->
      @Window val x = chartCalculator.domainRelative2windowX(data.model.valueX(lineIndex, pointIndex))
      @Window val y = chartCalculator.domainRelative2windowY(data.model.valueY(lineIndex, pointIndex))

      pointPainter.paintPoint(gc, x, y)
    }
  }

  class Data(
    var model: LinesChartModel
  )

  @StyleDsl
  open class Style {
    /**
     * Provides the point painters for the given line index
     */
    var pointPainters: MultiProvider<LinesChartModelIndex, PointPainter> = MultiProvider.always(PointStylePainter(PointStyle.Dot, 2.0, snapXValues = false, snapYValues = false))

    /**
     * Provides the line painters for the given line index
     */
    var linePainters: MultiProvider<LinesChartModelIndex, LinePainter> = MultiProvider.always(DirectLinePainter(snapXValues = false, snapYValues = false))

    /**
     * Provides the line style for the given line index
     */
    var lineStyles: MultiProvider<LinesChartModelIndex, LineStyle> = MultiProvider.forListModulo(Palette.chartColors.map { LineStyle(color = it) })
  }
}

/**
 * Adds a [LineChartLayer] with the given [model]
 */
fun Layers.addLineChart(model: LinesChartModel): LineChartLayer {
  val layer = LineChartLayer(LineChartLayer.Data(model))
  addLayer(layer)
  return layer
}

/**
 * Refers to the index within the [LinesChartModel]
 */
@MultiProviderIndexContextAnnotation
@Retention(AnnotationRetention.SOURCE)
annotation class LinesChartModelIndex {
}
