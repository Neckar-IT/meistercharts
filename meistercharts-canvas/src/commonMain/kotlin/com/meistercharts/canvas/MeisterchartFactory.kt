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
package com.meistercharts.canvas

import com.meistercharts.Meistercharts

/**
 * Contains platform dependent methods / factories.
 *
 * It is suggested to get the current factory using [Meistercharts.meisterchartFactory].
 *
 * It is necessary to
 * call `MeisterChartsPlatform.init()` first
 *
 */
interface MeisterchartFactory {
  /**
   * Creates a new chart instance
   */
  fun createChart(
    chartSupport: ChartSupport,
    /**
     * Only used for debugging purposes
     */
    description: String,
  ): Meisterchart

  /**
   * The platform dependent canvas factory
   */
  val canvasFactory: CanvasFactory

  companion object {
    /**
     * Returns an instance for the platform dependent chart factory.
     *
     * It is necessary to
     * call `MeisterChartsPlatform.init()` first
     */
    fun get(): MeisterchartFactory {
      return Meistercharts.meisterchartFactory ?: throw IllegalStateException("No meisterChartsFactory set - please call MeisterChartsPlatform.init()")
    }
  }
}
