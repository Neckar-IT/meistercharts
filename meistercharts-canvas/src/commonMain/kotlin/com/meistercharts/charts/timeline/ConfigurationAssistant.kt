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

import com.meistercharts.zoom.ZoomAndTranslationModifier
import com.meistercharts.zoom.DelegatingZoomAndTranslationDefaults
import com.meistercharts.zoom.FittingWithMargin
import com.meistercharts.zoom.ZoomAndTranslationModifiersBuilder
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.algorithms.tile.MinDistanceSamplingPeriodCalculator
import com.meistercharts.algorithms.tile.withMinimum
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.asDoubleProvider
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class ConfigurationAssistant(
  val calculator: ConfigurationCalculator,
) {

  constructor(
    durationBetweenSamples: @ms Double,
    gapFactor: Double = 12.0,
    manualMinDistanceBetweenSamples: @px Double? = null,
    manualMaxDistanceBetweenSamples: @px Double? = null,
    manualIdealDistanceBetweenSamples: @px Double? = null,
    manualMinZoom: Double? = null,
    manualMaxZoom: Double? = null,
  ) : this(
    calculator = ConfigurationCalculator(
      durationBetweenSamples = durationBetweenSamples,
      gapFactor = gapFactor,
      manualMinDistanceBetweenSamples = manualMinDistanceBetweenSamples,
      manualMaxDistanceBetweenSamples = manualMaxDistanceBetweenSamples,
      manualIdealDistanceBetweenSamples = manualIdealDistanceBetweenSamples,
      manualMinZoom = manualMinZoom,
      manualMaxZoom = manualMaxZoom,
    ),
  )

  /**
   * Provides the target time for the cross wire
   */
  var crossWireTargetTime: @ms DoubleProvider = DoubleProvider.nowMillis

  fun useCandles(candleWidth: @px Double? = null) {
    setMinDistanceBetweenDataPoints(candleWidth ?: 8.0)
  }

  fun useDots(dotDiameter: @px Double? = null) {
    setMinDistanceBetweenDataPoints((dotDiameter ?: 4.0) + 2.0) // 2px margin between the points
  }

  fun usePlainLine() {
    setMinDistanceBetweenDataPoints(null)
  }

  //fun showAllData(): ConfigurationAssistant {
  //  return setDefaultContentAreaDuration(TODO("calculate"))
  //}

  fun showLiveData() {
    crossWireTargetTime = DoubleProvider.nowMillis
  }

  fun setCandleMinWidth(minWidth: @px Double) {
    return setMinDistanceBetweenDataPoints(minWidth)
  }

  fun setCandleMaxWidth(maxWidth: @px Double) {
    return setMaxDistanceBetweenDataPoints(maxWidth)
  }

  fun setMinDistanceBetweenDataPoints(minDistance: @px Double?) {
    calculator.manualMinDistanceBetweenSamples = minDistance
  }

  fun setMaxDistanceBetweenDataPoints(maxDistance: @px Double?) {
    calculator.manualMaxDistanceBetweenSamples = maxDistance
  }

  fun setDurationBetweenSamples(duration: @ms Double) {
    calculator.durationBetweenSamples = duration
  }

  fun setIdealDistanceBetweenSamples(idealDistance: @px Double?) {
    calculator.manualIdealDistanceBetweenSamples = idealDistance
  }

  /**
   * Bei "krummen" Werten, die nicht zur Sampling Period passen, treffen wir die Entscheidung!
   */
  fun setDataPointCountPerSecond(perSecond: Double) {
    calculator.durationBetweenSamples = (1.0 / perSecond).seconds.toDouble(DurationUnit.MILLISECONDS)
  }

  fun setDataPointCountPerMinute(perMinute: Double) {
    calculator.durationBetweenSamples = (1.0 / perMinute).minutes.toDouble(DurationUnit.MILLISECONDS)
  }

  fun setDataPointCountPerHour(perHour: Double) {
    calculator.durationBetweenSamples = (1.0 / perHour).hours.toDouble(DurationUnit.MILLISECONDS)
  }

  fun setDataPointCountPerDay(perDay: Double) {
    calculator.durationBetweenSamples = (1.0 / perDay).days.toDouble(DurationUnit.MILLISECONDS)
  }

  fun setDataPointCountPerMonth(perMonth: Double) {
    calculator.durationBetweenSamples = (30.5 / perMonth).days.toDouble(DurationUnit.MILLISECONDS)
  }

  fun setDataPointCountPerYear(perYear: Double) {
    calculator.durationBetweenSamples = (365.25 / perYear).days.toDouble(DurationUnit.MILLISECONDS)
  }

  @OnlyForPros
  fun setSamplingPeriod(durationBetweenRecordedDataPoints: @ms Double) {
    calculator.durationBetweenSamples = durationBetweenRecordedDataPoints
  }

  /**
   * Sets the fixed cross wire date
   */
  fun setFixedCrossWireDate(crossWireDate: @ms Double) {
    this.crossWireTargetTime = crossWireDate.asDoubleProvider()
  }

  fun setGapFactor(gapFactor: Double) {
    calculator.gapFactor = gapFactor
  }

  fun applyToStorage(inMemoryStorage: InMemoryHistoryStorage, guaranteedHistoryLength: Duration) {
    val naturalHistoryBucketRange = calculator.historyBucketRange
    inMemoryStorage.naturalSamplingPeriod = naturalHistoryBucketRange.samplingPeriod
    inMemoryStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(guaranteedHistoryLength, naturalHistoryBucketRange)
  }

  fun applyToGestalt(gestalt: TimeLineChartGestalt) {
    logger.debug {
      "applyToGestalt: $calculator"
    }

    gestalt.data.minimumSamplingPeriod = calculator.recordingSamplingPeriod
    gestalt.historyRenderPropertiesCalculatorLayer.samplingPeriodCalculator = MinDistanceSamplingPeriodCalculator(calculator.minDistanceBetweenSamples).withMinimum(calculator.recordingSamplingPeriod)
    //gestalt.historyRenderPropertiesCalculatorLayer.samplingPeriodCalculator = MaxDistanceSamplingPeriodCalculator(maxDistanceBetweenDataPoints).withMinimum(recordingSamplingPeriod)
    gestalt.style.contentAreaDuration = calculator.contentAreaDuration
    gestalt.data.historyGapCalculator = DefaultHistoryGapCalculator(calculator.gapFactor)

    gestalt.chartSupport().let {
      it.zoomAndTranslationSupport.zoomAndTranslationDefaults = createZoomAndTranslationDefaults(gestalt)
      it.zoomAndTranslationSupport.zoomAndTranslationModifier = createZoomAndTranslationModifier(gestalt)
    }
  }

  /**
   * Creates the zoom and translation defaults for the chart
   */
  fun createZoomAndTranslationDefaults(gestalt: TimeLineChartGestalt): DelegatingZoomAndTranslationDefaults {
    return DelegatingZoomAndTranslationDefaults(
      xAxisDelegate = MoveTimeUnderCrossWire.create(gestalt),
      yAxisDelegate = FittingWithMargin { gestalt.viewportSupport.decimalsAreaViewportMargin() },
    )
  }

  fun createZoomAndTranslationModifier(gestalt: TimeLineChartGestalt): ZoomAndTranslationModifier {
    return ZoomAndTranslationModifiersBuilder()
      .minZoom(calculator.createMinZoomXProvider(gestalt), 0.000001.asDoubleProvider())
      .maxZoom(calculator.createMaxZoomXProvider(), 500.0.asDoubleProvider())
      .build()
  }


  companion object {

    fun withDataPointCountPerSecond(perSecond: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenSamples = (1.0 / perSecond).seconds.toDouble(DurationUnit.MILLISECONDS))
    }

    fun withDataPointCountPerMinute(perMinute: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenSamples = (1.0 / perMinute).minutes.toDouble(DurationUnit.MILLISECONDS))
    }

    fun withDataPointCountPerHour(perHour: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenSamples = (1.0 / perHour).hours.toDouble(DurationUnit.MILLISECONDS))
    }

    fun withDataPointCountPerDay(perDay: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenSamples = (1.0 / perDay).days.toDouble(DurationUnit.MILLISECONDS))
    }

    fun withDataPointCountPerMonth(perMonth: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenSamples = (30.5 / perMonth).days.toDouble(DurationUnit.MILLISECONDS))
    }

    fun withDataPointCountPerYear(perYear: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenSamples = (365.25 / perYear).days.toDouble(DurationUnit.MILLISECONDS))
    }

    @OnlyForPros
    fun withDurationBetweenSamples(durationBetweenSamples: Duration): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenSamples = durationBetweenSamples.toDouble(DurationUnit.MILLISECONDS))
    }

    @OnlyForPros
    fun withSamplingPeriod(samplingPeriod: SamplingPeriod): ConfigurationAssistant {
      return withDurationBetweenSamples(samplingPeriod.distance.milliseconds)
    }

    fun hereIsMyDataThatsAll(myData: Any, targetWindowWidth: @px Double): ConfigurationAssistant {
      TODO("Not yet implemented")
      return ConfigurationAssistant(durationBetweenSamples = TODO("calculate"))
      //.showAllData()
    }

    private val logger = LoggerFactory.getLogger("com.meistercharts.charts.timeline.ConfigurationAssistant")
  }
}
