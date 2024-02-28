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
package com.meistercharts.algorithms.layers.scatterplot

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.linechart.PointStyle
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.design.CurrentTheme
import com.meistercharts.painter.PointPainter
import com.meistercharts.painter.PointStylePainter
import com.meistercharts.provider.ValueRangeProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.fastForEachIndexed

/**
 * The layer painting the scatter plot
 */
class ScatterPlotLayer(
  val configuration: Configuration,
  addtionalConfiguration: Configuration.() -> Unit = {}
) : AbstractLayer() {

  constructor(
    xValues: @Domain DoublesProvider,
    yValues: @Domain DoublesProvider,
    valueRangeXProvider: ValueRangeProvider,
    valueRangeYProvider: ValueRangeProvider,
    addtionalConfiguration: Configuration.() -> Unit = {}
  ): this(Configuration(xValues, yValues, valueRangeXProvider, valueRangeYProvider), addtionalConfiguration)

  init {
    configuration.addtionalConfiguration()
  }

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    val valueRangeX = configuration.valueRangeXProvider()
    val valueRangeY = configuration.valueRangeYProvider()

    configuration.xValues.fastForEachIndexed { index, xValue: @Domain Double ->
      val yValue: @Domain Double = configuration.yValues.valueAt(index)

      val x = chartCalculator.domain2windowX(xValue, valueRangeX)
      val y = chartCalculator.domain2windowY(yValue, valueRangeY)

      configuration.pointPainter.paintPoint(gc, x, y)
    }
  }

  @ConfigurationDsl
  open class Configuration(
    /**
     * Provides the x values.
     * Must be of same size as [yValues]
     */
    var xValues: @Domain DoublesProvider,
    /**
     * Provides the y values
     * Must be of same size as [xValues]
     */
    var yValues: @Domain DoublesProvider,

    /**
     * Provides the value range for the x axis
     */
    val valueRangeXProvider: ValueRangeProvider,
    /**
     * Provides the value range for the y axis
     */
    val valueRangeYProvider: ValueRangeProvider
  ) {
    /**
     * The point painter that is used to point the paints
     */
    var pointPainter: PointPainter = object : PointPainter {
      val delegate = PointStylePainter(PointStyle.Cross, 1.0, false, false)

      override fun paintPoint(gc: CanvasRenderingContext, x: @Window Double, y: @Window Double) {
        gc.stroke(
          if (x < gc.width / 2.0) {
            if (y < gc.height / 2.0) {
              CurrentTheme.primaryColor
            } else {
              CurrentTheme.secondaryColor
            }
          } else {
            if (y < gc.height / 2.0) {
              CurrentTheme.primaryColorLighter
            } else {
              CurrentTheme.primaryColorDarker
            }
          }
        )

        delegate.paintPoint(gc, x, y)
      }
    }
  }
}
