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
package com.meistercharts.fx

import com.meistercharts.canvas.CanvasFactory
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.MeisterchartFactory

/**
 * Implementation of MeisterChartsFactory that creates instances of MeisterChartFX
 * using a CanvasFactoryFX to create the chart canvas.
 */
class MeisterchartFactoryFX : MeisterchartFactory {
  /**
   * The CanvasFactory used to create the chart canvas.
   */
  override val canvasFactory: CanvasFactory = CanvasFactoryFX()

  /**
   * Creates a new instance of MeisterChartFX using the given ChartSupport and description.
   *
   * @param chartSupport the ChartSupport object to use for the chart.
   * @param description a String describing the chart.
   * @return a new instance of MeisterChartFX.
   */
  override fun createChart(chartSupport: ChartSupport, description: String): MeisterchartFX {
    return MeisterchartFX(chartSupport, description)
  }
}
