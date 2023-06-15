package com.meistercharts.charts.timeline

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.SamplingPeriod
import korlibs.time.DateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ConfigurationAssistantTest {

  private lateinit var defaultAssistant: ConfigurationAssistant

  @BeforeEach
  fun setUpAssistant() {
    defaultAssistant = ConfigurationAssistant.withDurationBetweenRecordedDataPoints(200.milliseconds)
  }


  //////////////////////////////////////
  //Eigenschaften der Daten selbst
  //////////////////////////////////////
  @Test
  fun testApiEndUserDataYear() {
    val assistant = ConfigurationAssistant.withDataPointCountPerYear(3.toDouble())
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(2.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(50.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(4.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(30437.5.days)
    assertThat(calculator.minPointsPer1000px).isEqualTo(20.0)
    assertThat(calculator.minContentAreaDuration).isEqualTo(2435.days)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.Every90Days)
    assertThat(calculator.maxZoomX()).isEqualTo(12.5)
  }

  @Test
  fun testApiEndUserDataMonth() {
    val assistant = ConfigurationAssistant.withDataPointCountPerMonth(15.25)
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(2.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(50.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(4.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(500.days)
    assertThat(calculator.minPointsPer1000px).isEqualTo(20.0)
    assertThat(calculator.minContentAreaDuration).isEqualTo(40.days)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.Every24Hours)
    assertThat(calculator.maxZoomX()).isEqualTo(12.5)
  }

  @Test
  fun testApiEndUserDataDay() {
    val assistant = ConfigurationAssistant.withDataPointCountPerDay(10.toDouble())
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(2.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(50.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(4.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(25.days)
    assertThat(calculator.minPointsPer1000px).isEqualTo(20.0)
    assertThat(calculator.minContentAreaDuration).isEqualTo(2.days)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.EveryHour)
    assertThat(calculator.maxZoomX()).isEqualTo(12.5)
  }

  @Test
  fun testApiEndUserDataHour() {
    val assistant = ConfigurationAssistant.withDataPointCountPerHour(10.toDouble())
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(2.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(50.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(4.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(25.hours)
    assertThat(calculator.minPointsPer1000px).isEqualTo(20.0)
    assertThat(calculator.minContentAreaDuration).isEqualTo(2.hours)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.EveryMinute)
    assertThat(calculator.maxZoomX()).isEqualTo(12.5)
  }

  @Test
  fun testApiEndUserDataMinute() {
    val assistant = ConfigurationAssistant.withDataPointCountPerMinute(10.toDouble())
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(2.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(50.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(4.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(25.minutes)
    assertThat(calculator.minPointsPer1000px).isEqualTo(20.0)
    assertThat(calculator.minContentAreaDuration).isEqualTo(2.minutes)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.EverySecond)
    assertThat(calculator.maxZoomX()).isEqualTo(12.5)
  }

  @Test
  fun testApiEndUserDataMinuteWithDots() {
    val assistant = ConfigurationAssistant.withDataPointCountPerMinute(10.toDouble())
    assistant.useDots()
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(6.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(50.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(12.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(8.minutes + 20.seconds)
    assertThat(calculator.minPointsPer1000px).isEqualTo(20.0)
    assertThat(calculator.minContentAreaDuration).isEqualTo(2.minutes)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.EverySecond)
    assertThat(calculator.maxZoomX()).isEqualTo(25.0 / 6.0)
  }

  @Test
  fun testApiEndUserDataSecond() {
    val assistant = ConfigurationAssistant.withDataPointCountPerSecond(10.toDouble())
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(2.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(50.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(4.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(25.seconds)
    assertThat(calculator.minPointsPer1000px).isEqualTo(20.0)
    assertThat(calculator.minContentAreaDuration).isEqualTo(2.seconds)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.EveryHundredMillis)
    assertThat(calculator.maxZoomX()).isEqualTo(12.5)
  }

  @Test
  fun testApiEndUserDataPeroid() {
    val assistant = ConfigurationAssistant.withDurationBetweenRecordedDataPoints(500.milliseconds)
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(2.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(50.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(4.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(125.seconds)
    assertThat(calculator.minPointsPer1000px).isEqualTo(20.0)
    assertThat(calculator.minContentAreaDuration).isEqualTo(10.seconds)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.EveryHundredMillis)
    assertThat(calculator.maxZoomX()).isEqualTo(12.5)
    assertThat(calculator.getMinZoomX(18.minutes)).isCloseTo(0.05787, 0.000001)
  }

  //////////////////////////////////////
  //Konfiguration der Zeit
  //////////////////////////////////////
  @Test
  fun testApiEndUserTimeAll() {
    //Visible duration
    val assistant = defaultAssistant//.showAllData() //Update on window size change, too
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(2.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(50.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(4.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(50.seconds)
    assertThat(calculator.minPointsPer1000px).isEqualTo(20.0)
    assertThat(calculator.minContentAreaDuration).isEqualTo(4.seconds)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.EveryHundredMillis)
    assertThat(calculator.maxZoomX()).isEqualTo(12.5)
  }

  @Test
  fun testApiEndUserTimeLive() {
    //TODO: window width? (target screen size?)
    val assistant = defaultAssistant
    assistant.showLiveData() //always be at now()
    assistant.setDefaultCrossWireDate(DateTime(2023, 3, 5, 7, 8, 9))
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(2.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(50.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(4.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(50.seconds)
    assertThat(calculator.minPointsPer1000px).isEqualTo(20.0)
    assertThat(calculator.minContentAreaDuration).isEqualTo(4.seconds)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.EveryHundredMillis)
    assertThat(calculator.maxZoomX()).isEqualTo(12.5)
  }

  //////////////////////////////////////
  //Konfiguration des Aussehens
  //////////////////////////////////////
  @Test
  fun testApiEndUserAppearance() {
    val assistant = defaultAssistant
    //.showMinMaxAreas()
    assistant.useDots()
    assistant.setMinDistanceBetweenDataPoints(5.toDouble())
    assistant.setMaxDistanceBetweenDataPoints(5.toDouble())
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(5.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(5.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(5.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(40.seconds)
    assertThat(calculator.minPointsPer1000px).isEqualTo(200.0)
    assertThat(calculator.minContentAreaDuration).isEqualTo(40.seconds)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.EveryHundredMillis)
    assertThat(calculator.maxZoomX()).isEqualTo(1.0)
  }

  @Test
  fun testApiEndUserAppearanceCandles() {
    val assistant = defaultAssistant
    assistant.useCandles()
    assistant.setCandleMinWidth(5.toDouble())
    assistant.setCandleMaxWidth(15.toDouble())
    val calculator = assistant.calculator

    assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(5.0)
    assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(15.0)
    assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(10.0)
    assertThat(calculator.contentAreaDuration).isEqualTo(20.seconds)
    assertThat(calculator.minPointsPer1000px).isEqualTo(200.0 / 3)
    assertThat(calculator.minContentAreaDuration).isEqualTo((40.0 / 3.0).seconds)
    assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.EveryHundredMillis)
    assertThat(calculator.maxZoomX()).isCloseTo(1.5, 0.000001)
  }

  //////////////////////////////////////////////////////
  // Scenario
  //////////////////////////////////////////////////////
  //@Test
  //fun testApiEndUserScenario() {
  //  val assistant = ConfigurationAssistant.hereIsMyDataThatsAll("mydata", defaultTargetWindowWidth)
  //  val calculator = assistant.calculator
  //
  //  assertThat(calculator.minDistanceBetweenDataPoints).isEqualTo(2.0)
  //  assertThat(calculator.maxDistanceBetweenDataPoints).isEqualTo(50.0)
  //  assertThat(calculator.idealDistanceBetweenDataPoints).isEqualTo(4.0)
  //  assertThat(calculator.contentAreaDuration).isEqualTo(125.seconds)
  //  assertThat(calculator.minPointsPer1000px).isEqualTo(20.0)
  //  assertThat(calculator.minContentAreaDuration).isEqualTo(10.seconds)
  //  assertThat(calculator.recordingSamplingPeriod).isEqualTo(SamplingPeriod.EveryHundredMillis)
  //  assertThat(calculator.maxZoom).isEqualTo(12.5)
  //}

}
