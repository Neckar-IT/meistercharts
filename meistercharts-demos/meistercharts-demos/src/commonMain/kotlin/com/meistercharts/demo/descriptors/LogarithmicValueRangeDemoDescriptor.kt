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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.LogarithmicValueRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.TickProvider
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.createGrid
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import it.neckar.open.collections.fastForEach
import kotlin.math.pow

/**
 *
 */
class LogarithmicValueRangeDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Logarithmic Value Range"

  override val category: DemoCategory = DemoCategory.Calculations

  private val values: DoubleArray = IntRange(-7, 20).map { 2.0.pow(it) }.toDoubleArray()

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val myLogValueRangeLayer = MyLogValueRangeLayer(values)

        val valueAxisLayer = ValueAxisLayer(ValueAxisLayer.Data { myLogValueRangeLayer.valueRange }) {
          ticks = TickProvider { _, _, _, _, _ -> values }
        }
        val gridLayer = valueAxisLayer.createGrid()


        configure {
          layers.addClearBackground()
          layers.addLayer(gridLayer)
          layers.addLayer(myLogValueRangeLayer)
          layers.addLayer(valueAxisLayer)
        }
      }
    }
  }
}

class MyLogValueRangeLayer(private val yValues: DoubleArray) : AbstractLayer() {
  var valueRange: LogarithmicValueRange = ValueRange.logarithmic(0.1, 1000.0)

  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    yValues.fastForEach { domain ->
      val windowX = chartCalculator.domain2windowX(domain, valueRange)
      val windowY = chartCalculator.domain2windowY(domain, valueRange)
      gc.paintMark(windowX, windowY)
    }
  }
}


