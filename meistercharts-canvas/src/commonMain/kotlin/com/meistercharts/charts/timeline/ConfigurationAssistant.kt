package com.meistercharts.charts.timeline

import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.impl.DelegatingZoomAndTranslationDefaults
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.impl.MoveDomainValueToLocation
import com.meistercharts.algorithms.impl.ZoomAndTranslationModifiersBuilder
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.algorithms.tile.MinDistanceSamplingPeriodCalculator
import com.meistercharts.algorithms.tile.withMinimum
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.asDoubleProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import korlibs.time.DateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class ConfigurationAssistant(
  val calculator: ConfigurationCalculator,
  var crossWireDate: DateTime? = null,
) {

  constructor(
    durationBetweenRecordedDataPoints: Duration,
    gapFactor: Double = 12.0,
    manualMinDistanceBetweenDataPoints: @px Double? = null,
    manualMaxDistanceBetweenDataPoints: @px Double? = null,
    manualIdealDistanceBetweenDataPoints: @px Double? = null,
    manualMinZoom: Double? = null,
    manualMaxZoom: Double? = null,
    crossWireDate: DateTime? = null,
  ) : this(
    calculator = ConfigurationCalculator(
      durationBetweenRecordedDataPoints = durationBetweenRecordedDataPoints,
      gapFactor = gapFactor,
      manualMinDistanceBetweenDataPoints = manualMinDistanceBetweenDataPoints,
      manualMaxDistanceBetweenDataPoints = manualMaxDistanceBetweenDataPoints,
      manualIdealDistanceBetweenDataPoints = manualIdealDistanceBetweenDataPoints,
      manualMinZoom = manualMinZoom,
      manualMaxZoom = manualMaxZoom,
    ),
    crossWireDate = crossWireDate,
  )

  val crossWireTargetTimeProvider: DoubleProvider
    get() = DoubleProvider { crossWireDate?.unixMillisDouble ?: nowMillis() }


  fun useCandles(candleWidth: @px Double? = null) {
    setMinDistanceBetweenDataPoints(candleWidth ?: 8.0)
  }

  fun useDots(dotDiameter: @px Double? = null) {
    setMinDistanceBetweenDataPoints((dotDiameter ?: 4.0) + 2.0)
  }

  fun usePlainLine() {
    setMinDistanceBetweenDataPoints(null)
  }

  //fun showAllData(): ConfigurationAssistant {
  //  return setDefaultContentAreaDuration(TODO("calculate"))
  //}

  fun showLiveData() {
    crossWireDate = null
  }

  fun setCandleMinWidth(minWidth: @px Double) {
    return setMinDistanceBetweenDataPoints(minWidth)
  }

  fun setCandleMaxWidth(maxWidth: @px Double) {
    return setMaxDistanceBetweenDataPoints(maxWidth)
  }

  fun setMinDistanceBetweenDataPoints(minDistance: @px Double?) {
    calculator.manualMinDistanceBetweenDataPoints = minDistance
  }

  fun setMaxDistanceBetweenDataPoints(maxDistance: @px Double?) {
    calculator.manualMaxDistanceBetweenDataPoints = maxDistance
  }

  fun setDurationBetweenRecordedDataPoints(duration: @ms Duration) {
    calculator.durationBetweenRecordedDataPoints = duration
  }

  /**
   * Bei "krummen" Werten, die nicht zur Sampling Period passen, treffen wir die Entscheidung!
   */
  fun setDataPointCountPerSecond(perSecond: Double) {
    calculator.durationBetweenRecordedDataPoints = (1.0 / perSecond).seconds
  }

  fun setDataPointCountPerMinute(perMinute: Double) {
    calculator.durationBetweenRecordedDataPoints = (1.0 / perMinute).minutes
  }

  fun setDataPointCountPerHour(perHour: Double) {
    calculator.durationBetweenRecordedDataPoints = (1.0 / perHour).hours
  }

  fun setDataPointCountPerDay(perDay: Double) {
    calculator.durationBetweenRecordedDataPoints = (1.0 / perDay).days
  }

  fun setDataPointCountPerMonth(perMonth: Double) {
    calculator.durationBetweenRecordedDataPoints = (30.5 / perMonth).days
  }

  fun setDataPointCountPerYear(perYear: Double) {
    calculator.durationBetweenRecordedDataPoints = (365.25 / perYear).days
  }

  @OnlyForPros
  fun setSamplingPeriod(durationBetweenRecordedDataPoints: Duration) {
    calculator.durationBetweenRecordedDataPoints = durationBetweenRecordedDataPoints
  }

  fun setDefaultCrossWireDate(crossWireDate: DateTime) {
    this.crossWireDate = crossWireDate
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
    gestalt.data.minimumSamplingPeriod = calculator.recordingSamplingPeriod
    gestalt.historyRenderPropertiesCalculatorLayer.samplingPeriodCalculator = MinDistanceSamplingPeriodCalculator(calculator.minDistanceBetweenDataPoints).withMinimum(calculator.recordingSamplingPeriod)
    //gestalt.historyRenderPropertiesCalculatorLayer.samplingPeriodCalculator = MaxDistanceSamplingPeriodCalculator(maxDistanceBetweenDataPoints).withMinimum(recordingSamplingPeriod)
    gestalt.style.contentAreaDuration = calculator.contentAreaDuration.toDouble(DurationUnit.MILLISECONDS)
    gestalt.data.historyGapCalculator = DefaultHistoryGapCalculator(calculator.gapFactor)


    gestalt.chartSupport().let {
      it.zoomAndTranslationSupport.zoomAndTranslationDefaults = createZoomAndTranslationDefaults(gestalt)
      it.zoomAndTranslationSupport.zoomAndTranslationModifier = createZoomAndTranslationModifier(gestalt)
    }
  }

  fun createZoomAndTranslationDefaults(gestalt: TimeLineChartGestalt): DelegatingZoomAndTranslationDefaults {
    return DelegatingZoomAndTranslationDefaults(
      MoveDomainValueToLocation(
        domainRelativeValueProvider = { gestalt.style.contentAreaTimeRange.time2relative(crossWireTargetTimeProvider()) },
        targetLocationProvider = { chartCalculator -> chartCalculator.windowRelative2WindowX(gestalt.style.crossWirePositionX) }
      ),
      FittingWithMargin { gestalt.viewportSupport.decimalsAreaViewportMargin() },
    )
  }

  fun createZoomAndTranslationModifier(gestalt: TimeLineChartGestalt): ZoomAndTranslationModifier {
    return ZoomAndTranslationModifiersBuilder()
      .minZoom(calculator.getMinZoomX(gestalt), 0.000001.asDoubleProvider()) //the x zoom works with 24 hours and 1 millis for the applied sampling rate
      .maxZoom(calculator.maxZoomX, 500.0.asDoubleProvider())
      .build()
  }


  companion object {

    fun withDataPointCountPerSecond(perSecond: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenRecordedDataPoints = (1.0 / perSecond).seconds)
    }

    fun withDataPointCountPerMinute(perMinute: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenRecordedDataPoints = (1.0 / perMinute).minutes)
    }

    fun withDataPointCountPerHour(perHour: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenRecordedDataPoints = (1.0 / perHour).hours)
    }

    fun withDataPointCountPerDay(perDay: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenRecordedDataPoints = (1.0 / perDay).days)
    }

    fun withDataPointCountPerMonth(perMonth: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenRecordedDataPoints = (30.5 / perMonth).days)
    }

    fun withDataPointCountPerYear(perYear: Double): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenRecordedDataPoints = (365.25 / perYear).days)
    }

    @OnlyForPros
    fun withDurationBetweenRecordedDataPoints(durationBetweenRecordedDataPoints: Duration): ConfigurationAssistant {
      return ConfigurationAssistant(durationBetweenRecordedDataPoints = durationBetweenRecordedDataPoints)
    }

    @OnlyForPros
    fun withSamplingPeriod(samplingPeriod: SamplingPeriod): ConfigurationAssistant {
      return withDurationBetweenRecordedDataPoints(samplingPeriod.distance.milliseconds)
    }

    fun hereIsMyDataThatsAll(myData: Any, targetWindowWidth: @px Double): ConfigurationAssistant {
      TODO("Not yet implemented")
      return ConfigurationAssistant(durationBetweenRecordedDataPoints = TODO("calculate"))
      //.showAllData()
    }

  }

}
