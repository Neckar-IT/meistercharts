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
package com.meistercharts.charts.timeline

import com.meistercharts.algorithms.ZoomLevelCalculator
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.SamplingPeriod
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

class ConfigurationCalculator(
  var durationBetweenSamples: @ms Double,
  var gapFactor: Double,

  @OnlyForPros
  var manualMinDistanceBetweenSamples: @px Double?,
  @OnlyForPros
  var manualMaxDistanceBetweenSamples: @px Double?,
  @OnlyForPros
  var manualIdealDistanceBetweenSamples: @px Double?,

  @OnlyForPros
  var manualMinZoom: Double?,
  @OnlyForPros
  var manualMaxZoom: Double?,
) {

  /**
   * The max value for the min zoom x level.
   * It does not make sense to set any values >= 1.0
   */
  var maxMinZoomX: Double = 0.25

  /**
   * The min value for the max zoom x level.
   * It does not make sens to set any values < 1.0
   */
  var minMaxZoomX: Double = 1.0

  val recordingSamplingPeriod: SamplingPeriod
    get() = SamplingPeriod.withMaxDuration(durationBetweenSamples)

  val minDistanceBetweenSamples: @px Double
    get() = (manualMinDistanceBetweenSamples ?: 2.0) // TODO: Calculate based on data

  val maxDistanceBetweenSamples: @px Double
    get() = (manualMaxDistanceBetweenSamples ?: 50.0) // TODO: Calculate based on data

  val idealDistanceBetweenSamples: @px Double
    get() = (manualIdealDistanceBetweenSamples ?: (minDistanceBetweenSamples * ZoomLevelCalculator.SQRT_2_TWICE))
      .coerceIn(minDistanceBetweenSamples, maxDistanceBetweenSamples)

  val maxPointsPer1000px: Double
    get() = 1000.0 / minDistanceBetweenSamples

  val minPointsPer1000px: Double
    get() = 1000.0 / maxDistanceBetweenSamples

  val idealPointsPer1000px: Double
    get() = 1000.0 / idealDistanceBetweenSamples

  val contentAreaDuration: @ms Double // Per 1000px
    get() = durationBetweenSamples * idealPointsPer1000px

  val minContentAreaDuration: @ms Double
    get() = minPointsPer1000px * durationBetweenSamples
  //val maxContentAreaDuration: Duration = TODO("Hängt dynamisch von der Menge der Daten ab")

  fun createMaxZoomXProvider(): DoubleProvider {
    return DoubleProvider { manualMaxZoom ?: (contentAreaDuration / minContentAreaDuration).coerceAtLeast(minMaxZoomX) }
  }

  fun createMinZoomXProvider(gestalt: TimeLineChartGestalt): @pct DoubleProvider {
    val historyStorage = gestalt.data.historyStorage

    return DoubleProvider {
      @MayBeNaN @ms val start = historyStorage.getStart()
      @MayBeNaN @ms val end = historyStorage.getEnd()

      if (start.isNaN() || end.isNaN()) {
        return@DoubleProvider 0.00001
      }

      require(end >= start) { "End <$end> must be >= start <$start>" }

      calculateMinZoomX(end - start)
    }
  }

  /**
   * Calculates the min zoom for the provided total duration
   */
  fun calculateMinZoomX(totalDuration: @ms Double): @pct Double {
    val maxContentAreaDuration = 2.0 * totalDuration
    return manualMinZoom ?: (contentAreaDuration / maxContentAreaDuration).coerceAtMost(maxMinZoomX)
  }

  val historyBucketRange: HistoryBucketRange
    get() = HistoryBucketRange.find(recordingSamplingPeriod)

  /**
   * TODO:
   * Distanz zwischen Punkten, die eintreten darf bevor es als Lücke gesehen wird
   */
  val gapDuration: @ms Double
    get() = durationBetweenSamples * gapFactor


  override fun toString(): String {
    return "ConfigurationCalculator(durationBetweenSamples=$durationBetweenSamples\ngapFactor=$gapFactor\nmanualMinDistanceBetweenSamples=$manualMinDistanceBetweenSamples\n" +
      "manualMaxDistanceBetweenSamples=$manualMaxDistanceBetweenSamples\nmanualIdealDistanceBetweenSamples=$manualIdealDistanceBetweenSamples\nmanualMinZoom=$manualMinZoom\n" +
      "manualMaxZoom=$manualMaxZoom\nrecordingSamplingPeriod=$recordingSamplingPeriod\nminDistanceBetweenSamples=$minDistanceBetweenSamples\nmaxDistanceBetweenSamples=$maxDistanceBetweenSamples\n" +
      "idealDistanceBetweenSamples=$idealDistanceBetweenSamples\nmaxPointsPer1000px=$maxPointsPer1000px\nminPointsPer1000px=$minPointsPer1000px\nidealPointsPer1000px=$idealPointsPer1000px\n" +
      "contentAreaDuration=$contentAreaDuration\nminContentAreaDuration=$minContentAreaDuration\nmaxZoomX=${createMaxZoomXProvider().invoke()}\nhistoryBucketRange=$historyBucketRange\ngapDuration=$gapDuration)"
  }
}
