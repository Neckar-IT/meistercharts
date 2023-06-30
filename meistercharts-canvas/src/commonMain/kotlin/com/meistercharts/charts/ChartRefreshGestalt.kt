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

import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.translateOverTime
import it.neckar.open.observable.ObservableObject

/**
 * A gestalt that provides configurations related to repainting the chart
 */
class ChartRefreshGestalt(
  targetRefreshRate: TargetRefreshRate = TargetRefreshRate.veryFast60,
) : ChartGestalt {

  val configuration: Configuration = Configuration().also {
    it.targetRefreshRate = targetRefreshRate
  }

  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    meisterChartBuilder.configure {
      configuration.targetRefreshRateProperty.consumeImmediately {
        chartSupport.targetRenderRate = it
      }

      configuration.chartAnimationRoundingStrategyProperty.consumeImmediately {
        chartSupport.translateOverTime.roundingStrategy = it
      }
    }
  }

  class Configuration {
    /**
     * The target refresh rate for the repaints
     */
    val targetRefreshRateProperty: ObservableObject<TargetRefreshRate> = ObservableObject(TargetRefreshRate.veryFast60)
    var targetRefreshRate: TargetRefreshRate by targetRefreshRateProperty

    /**
     * The animation rounding strategy. Mostly relevant when animating by time
     */
    val chartAnimationRoundingStrategyProperty: ObservableObject<RoundingStrategy> = ObservableObject(RoundingStrategy.quarter)
    var chartAnimationRoundingStrategy: RoundingStrategy by chartAnimationRoundingStrategyProperty
  }
}

