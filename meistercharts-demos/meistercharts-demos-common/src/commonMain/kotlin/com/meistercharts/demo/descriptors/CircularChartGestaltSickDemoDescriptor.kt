/*
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


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.circular.FixedPixelsGap
import com.meistercharts.charts.CircularChartGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType

/**
 */
class CircularChartGestaltSickDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("e901cdde-5d06-480a-aa62-111cf586bbb4")
  override val name: String = "Circular Chart (SICK)"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "A circular chart in the SICK AG design"
  override val category: DemoCategory = DemoCategory.Automation

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val circularChartGestalt = CircularChartGestalt().apply {
          circularChartLayer.configuration.outerCircleWidth = 6.0
          circularChartLayer.configuration.gapInnerOuter = 6.0
          circularChartLayer.configuration.innerCircleWidth = 19.0
          circularChartLayer.configuration.outerCircleValueGap = FixedPixelsGap(2.0)
        }
        circularChartGestalt.configure(this)
      }
    }
  }
}
