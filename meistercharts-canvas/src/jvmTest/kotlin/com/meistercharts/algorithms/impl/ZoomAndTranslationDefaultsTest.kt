package com.meistercharts.algorithms.impl

import assertk.*
import assertk.assertions.*
import assertk.assertions.support.*
import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.ZoomAndTranslationSupport
import com.meistercharts.model.Distance
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class ZoomAndTranslationDefaultsTest {
  lateinit var chartState: DefaultChartState
  lateinit var zoomAndTranslationSupport: ZoomAndTranslationSupport
  lateinit var chartCalculator: ChartCalculator

  @BeforeEach
  fun setUp() {
    chartState = DefaultChartState()
    zoomAndTranslationSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)
    chartCalculator = zoomAndTranslationSupport.chartCalculator

    assertThat(chartState.windowSize).isZero()
  }

  @Test
  fun testFittingWithMarginPercentage() {
    assertThat(chartState.windowSize).isZero()

    FittingWithMarginPercentage(0.0, 0.0).let {
      assertThat(it.defaultZoom(chartCalculator)).isEqualTo(Zoom.default)
      assertThat(it.defaultTranslation(chartCalculator)).isEqualTo(Distance.none)
    }

    FittingWithMarginPercentage(0.1, 0.1).let {
      assertThat(it.defaultZoom(chartCalculator)).isEqualTo(Zoom.of(0.9, 0.9))
      assertThat(it.defaultTranslation(chartCalculator)).isEqualTo(Distance.none)
    }
  }

  @Test
  fun testMoveDomainValueToLocation() {
    assertThat(chartState.windowSize).isZero()

    MoveDomainValueToLocation(
      domainRelativeValueProvider = { 0.8 },
      targetLocationProvider = { _ -> Double.NaN }
    ).let {
      assertThat(it.defaultZoom(chartCalculator)).isEqualTo(Zoom.default)
      assertThat(it.defaultTranslation(chartCalculator)).isEqualTo(Distance.zero)
    }
  }
}

private fun Assert<Size>.isZero() = given { actual ->
  if (actual.isZero()) return
  expected("to be zero but was <$actual>")
}
