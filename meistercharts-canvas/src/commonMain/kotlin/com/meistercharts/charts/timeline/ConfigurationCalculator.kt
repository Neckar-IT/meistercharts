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
  var durationBetweenRecordedDataPoints: Duration,
  var gapFactor: Double,

  @OnlyForPros
  var manualMinDistanceBetweenDataPoints: @px Double?,
  @OnlyForPros
  var manualMaxDistanceBetweenDataPoints: @px Double?,
  @OnlyForPros
  var manualIdealDistanceBetweenDataPoints: @px Double?,

  @OnlyForPros
  var manualMinZoom: Double?,
  @OnlyForPros
  var manualMaxZoom: Double?,
) {

  val recordingSamplingPeriod: SamplingPeriod
    get() = SamplingPeriod.withMaxDuration(durationBetweenRecordedDataPoints)

  val minDistanceBetweenDataPoints: @px Double
    get() = manualMinDistanceBetweenDataPoints ?: 2.0 // TODO: Calculate based on data

  val maxDistanceBetweenDataPoints: @px Double
    get() = manualMaxDistanceBetweenDataPoints ?: 50.0 // TODO: Calculate based on data

  val idealDistanceBetweenDataPoints: @px Double
    get() = manualIdealDistanceBetweenDataPoints ?: (minDistanceBetweenDataPoints * ZoomLevelCalculator.SQRT_2_TWICE).coerceAtMost(maxDistanceBetweenDataPoints)

  val maxPointsPer1000px: Double
    get() = 1000.0 / minDistanceBetweenDataPoints

  val minPointsPer1000px: Double
    get() = 1000.0 / maxDistanceBetweenDataPoints

  val idealPointsPer1000px: Double
    get() = 1000.0 / idealDistanceBetweenDataPoints

  val contentAreaDuration: Duration // Per 1000px
    get() = durationBetweenRecordedDataPoints * idealPointsPer1000px

  val minContentAreaDuration: Duration
    get() = minPointsPer1000px * durationBetweenRecordedDataPoints
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
    get() = durationBetweenRecordedDataPoints * gapFactor

}
