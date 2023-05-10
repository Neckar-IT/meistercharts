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

import com.meistercharts.algorithms.layers.circular.FixedPixelsGap
import com.meistercharts.charts.CircularChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 */
class CircularChartGestaltSickDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Circular Chart (SICK)"

  //language=HTML
  override val description: String = "## Circular Chart - design by SICK AG"
  override val category: DemoCategory = DemoCategory.Automation

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val circularChartGestalt = CircularChartGestalt().apply {
          layer.style.outerCircleWidth = 6.0
          layer.style.gapInnerOuter = 6.0
          layer.style.innerCircleWidth = 19.0
          layer.style.outerCircleValueGap = FixedPixelsGap(2.0)
        }
        circularChartGestalt.configure(this)
      }
    }
  }
}