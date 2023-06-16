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
import com.meistercharts.history.InMemoryBookKeeping
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.times

class ConfigurationCalculator(
  var durationBetweenSamples: Duration,
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

  val contentAreaDuration: Duration // Per 1000px
    get() = durationBetweenSamples * idealPointsPer1000px

  val minContentAreaDuration: Duration
    get() = minPointsPer1000px * durationBetweenSamples
  //val maxContentAreaDuration: Duration = TODO("Hängt dynamisch von der Menge der Daten ab")

  val maxZoomX: DoubleProvider
    get() = DoubleProvider { manualMaxZoom ?: (contentAreaDuration / minContentAreaDuration) }

  fun getMinZoomX(gestalt: TimeLineChartGestalt): @pct DoubleProvider {
    val historyStorage = gestalt.data.historyStorage as InMemoryHistoryStorage
    val bookKeeping = historyStorage.bookKeeping

    return getMinZoomX(bookKeeping)
  }

  fun getMinZoomX(bookKeeping: InMemoryBookKeeping): @pct DoubleProvider {
    return DoubleProvider {
      bookKeeping.getTimeRange(historyBucketRange)?.let {
        getMinZoomX(it.span.milliseconds)
      } ?: 0.00001
    }
  }

  fun getMinZoomX(totalDuration: @ms Duration): @pct Double {
    val maxContentAreaDuration = 2.0 * totalDuration
    return manualMinZoom ?: (contentAreaDuration / maxContentAreaDuration)
  }

  val historyBucketRange: HistoryBucketRange
    get() = HistoryBucketRange.find(recordingSamplingPeriod)

  /**
   * TODO:
   * Distanz zwischen Punkten, die eintreten darf bevor es als Lücke gesehen wird
   */
  val gapDuration: Duration
    get() = durationBetweenSamples * gapFactor

}
