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
package com.meistercharts.charts

import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.translateOverTime

/**
 * A gestalt that provides configurations related to repainting the chart
 */
class ChartRefreshGestalt(
  /**
   * The target refresh rate
   */
  targetRefreshRate: TargetRefreshRate = TargetRefreshRate.veryFast60,
) : AbstractChartGestalt() {

  val configuration: Configuration = Configuration().also {
    it.targetRefreshRate = targetRefreshRate
  }

  @ConfigurationDsl
  inner class Configuration {
    /**
     * The target refresh rate for the repaints
     */
    var targetRefreshRate: TargetRefreshRate by chartSupport()::targetRenderRate

    /**
     * The animation rounding strategy. Mostly relevant when animating by time
     */
    var chartAnimationRoundingStrategy: RoundingStrategy by chartSupport().translateOverTime::roundingStrategy
  }
}

