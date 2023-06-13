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

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.UpdateReason
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Insets
import it.neckar.open.kotlin.lang.round
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.unit.si.ms

/**
 * Translates a canvas over time.
 * This service is registered as refresh listener at [ChartSupport.onRefresh].
 *
 * Do *not* create a new instance. Call [ChartSupport.translateOverTime] instead.
 *
 */
class ChartTranslateOverTimeService(val chartSupport: ChartSupport) : RefreshListener {
  /**
   * Whether the chart is currently animated
   */
  val animatedProperty: ObservableBoolean = ObservableBoolean(false).also {
    it.consume {
      requireNotNull(contentAreaTimeRangeX) {
        "contentAreaTimeRangeX must be set for the animation to work"
      }
    }
  }
  var animated: Boolean by animatedProperty

  /**
   * The time range that represents the content area.
   * If set to null the chart is not animated.
   *
   * This represents the time range that is visible for 100% of the content area.
   * The translation is set, that *now* is visible.
   * Usually the content area is translated to the left - often not even visible.
   */
  var contentAreaTimeRangeX: TimeRange? = null

  /**
   * The insets that are used to keep a distance to the (right) side of the window
   * Usually only [Insets.right] is used to determine the distance to the right side
   */
  var insets: @Zoomed Insets = Insets.of(10.0)

  /**
   * The rounding strategy that is used to round the translations used for the translation
   */
  var roundingStrategy: RoundingStrategy = RoundingStrategy.quarter

  override fun refresh(chartSupport: ChartSupport, frameTimestamp: @ms Double, refreshDelta: @ms Double) {
    if (!animated) {
      return
    }

    require(this.chartSupport == chartSupport) {
      "Invalid chart support. Was $chartSupport but expected ${this.chartSupport}"
    }

    //Calculate the new translation
    translateTo(frameTimestamp, reason = UpdateReason.Animation)
  }

  /**
   * Translates the chart to the given timestamp
   */
  fun translateTo(timestamp: @ms Double, reason: UpdateReason) {
    contentAreaTimeRangeX?.let {
      @TimeRelative val timeRelative = it.time2relative(timestamp)

      @Zoomed val newTranslation = -chartSupport.chartCalculator.domainRelative2zoomedX(timeRelative) + chartSupport.currentChartState.windowSize.width - insets.right
      chartSupport.zoomAndTranslationSupport.setWindowTranslationX(roundingStrategy.round(newTranslation), reason = reason)
    }
  }
}

/**
 * The rounding strategy that is used to round the translation.
 * This can help to avoid unnecessary repaints if the translation only changes insignificantly
 */
fun interface RoundingStrategy {
  /**
   * Rounds the translation
   */
  fun round(exactValue: Double): Double

  companion object {
    /**
     * Does not round at all
     */
    val exact: RoundingStrategy = RoundingStrategy { exactValue -> exactValue }

    /**
     * Rounds to the nearest full pixel
     */
    val round: RoundingStrategy = RoundingStrategy { exactValue -> exactValue.round() }

    /**
     * Rounds to half of a pixel
     */
    val half: RoundingStrategy = RoundingStrategy { exactValue -> (exactValue * 2).round() / 2.0 }

    /**
     * Rounds to a quarter pixel
     */
    val quarter: RoundingStrategy = RoundingStrategy { exactValue -> (exactValue * 4).round() / 4.0 }

    /**
     * Rounds to tenth of a pixel
     */
    val tenth: RoundingStrategy = RoundingStrategy { exactValue -> (exactValue * 10).round() / 10.0 }

    /**
     * The predefined rounding strategies
     */
    val predefined: List<RoundingStrategy> = listOf(exact, round, half, tenth)
  }
}
