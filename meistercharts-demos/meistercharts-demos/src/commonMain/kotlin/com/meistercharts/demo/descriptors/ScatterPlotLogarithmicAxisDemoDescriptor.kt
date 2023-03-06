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

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.axis.IntermediateValuesMode
import com.meistercharts.algorithms.axis.LinearAxisTickCalculator.calculateTickValues
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.TickProvider
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addShowZoomLevel
import com.meistercharts.charts.ScatterPlotGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import it.neckar.open.collections.fastMapDouble
import it.neckar.open.kotlin.lang.isPositive
import kotlin.math.log10
import kotlin.math.pow

class ScatterPlotLogarithmicAxisDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Scatter Plot - Logarithmic Axis"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        zoomAndTranslationDefaults {
          ZoomAndTranslationDefaults.tenPercentMargin
        }

        val valueRangeX = ValueRange.logarithmic(0.1, 100.0)
        val valueRangeY = ValueRange.logarithmic(0.1, 100.0)

        val scatterPlotGestalt = ScatterPlotGestalt(
          ScatterPlotGestalt.createDefaultData(valueRangeX, valueRangeY)
        ).also {
          it.valueAxisYLayer.style.applyLogarithmicScale()
          it.valueAxisXLayer.style.applyLogarithmicScale()
        }

        scatterPlotGestalt.configure(this)

        //Fix the value range
        scatterPlotGestalt.valueAxisXLayer.style.ticks = TickProvider { lowerValue, upperValue, maxTickCount, minTickDistance, axisEndConfiguration ->
          require(lowerValue.isPositive()) { "only positive values supported for logarithmic values but lower value was <$lowerValue>" }
          require(upperValue.isPositive()) { "only positive values supported for logarithmic values but upper value was <$upperValue>" }

          calculateTickValues(log10(lowerValue), log10(upperValue), axisEndConfiguration, maxTickCount, minTickDistance, IntermediateValuesMode.Also5and2)
            .fastMapDouble {
              10.0.pow(it)
            }
        }


        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addShowZoomLevel()
        }
      }
    }
  }
}
