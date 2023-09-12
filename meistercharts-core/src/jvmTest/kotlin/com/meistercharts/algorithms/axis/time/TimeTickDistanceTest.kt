package com.meistercharts.algorithms.axis.time

import assertk.*
import assertk.assertions.*
import com.meistercharts.axis.time.DistanceDays
import com.meistercharts.axis.time.DistanceHours
import com.meistercharts.axis.time.DistanceMillis
import com.meistercharts.axis.time.DistanceMinutes
import com.meistercharts.axis.time.DistanceMonths
import com.meistercharts.axis.time.DistanceSeconds
import com.meistercharts.axis.time.DistanceYears
import com.meistercharts.axis.time.TimeTickDistance
import it.neckar.datetime.minimal.LocalDate
import it.neckar.datetime.minimal.TimeConstants
import it.neckar.datetime.minimal.TimeZone
import it.neckar.datetime.minimal.fromMillisCurrentTimeZone
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.roundDownToBase
import it.neckar.open.test.utils.WithTimeZone
import it.neckar.open.time.formatUtcForDebug
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@WithTimeZone("Europe/Berlin")
class TimeTickDistanceTest {
  val time: @ms Double = 1546875919_123.987_345_678_901

  @BeforeEach
  fun setUp() {
    assertThat(time.formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.123Z")
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceYears1000000() {
    val start = -8.704446036793058E15
    assertThat(start.formatUtcForDebug()).isEqualTo("-273863-01-31T19:06:46.942Z")
    LocalDate.fromMillisCurrentTimeZone(start, TimeZone.NewYork).let {
      assertThat(it.year.value).isEqualTo(-273_863)
      assertThat(it.month.value).isEqualTo(1)
      assertThat(it.dayOfMonth.value).isEqualTo(31)
    }

    val end = 6.757436228806943E15
    assertThat(end.formatUtcForDebug()).isEqualTo("+216104-08-18T11:06:46.943Z")

    val timeTickDistance = DistanceYears(100_000)
    val ticks = timeTickDistance.calculateTicks(start, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(6)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("-300000-01-01T04:56:02.000Z") //precision error
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("-200000-01-01T04:56:02.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("-100000-01-01T04:56:02.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("0000-01-01T04:56:02.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("+100000-01-01T05:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)

    assertThat(timeTickDistance.formatAsOffset(ticks[0], I18nConfiguration.US)).isEqualTo("-300000")
    assertThat(timeTickDistance.formatAsOffset(ticks[1], I18nConfiguration.US)).isEqualTo("-200000")
    assertThat(timeTickDistance.formatAsOffset(ticks[2], I18nConfiguration.US)).isEqualTo("-100000")
    assertThat(timeTickDistance.formatAsOffset(ticks[3], I18nConfiguration.US)).isEqualTo("0")
    assertThat(timeTickDistance.formatAsOffset(ticks[4], I18nConfiguration.US)).isEqualTo("100000")
  }

  private fun verifyGlobalIndices(timeTickDistance: TimeTickDistance, ticks: @ms DoubleArrayList) {
    val globalIndices = ticks.toDoubleArray().map { tickMillis: @ms Double ->
      timeTickDistance.calculateEstimatedIndex(tickMillis, TimeZone.NewYork)
    }

    globalIndices.forEachIndexed { index, globalIndex ->
      if (index == 0) {
        //Skip the first entry
        return@forEachIndexed
      }

      globalIndices[index - 1].let { previousGlobalIndex ->
        assertThat(globalIndex, "Comparing $globalIndex @ $index").isNotEqualTo(previousGlobalIndex)
      }
    }
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceYears() {
    val timeTickDistance = DistanceYears(7)
    val ticks = timeTickDistance.calculateTicks(time, time + TimeConstants.millisPerYear * 30, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(5)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2016-01-01T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2023-01-01T05:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2030-01-01T05:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2037-01-01T05:00:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2044-01-01T05:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)

    assertThat(timeTickDistance.formatAsOffset(ticks[0], I18nConfiguration.US)).isEqualTo("2016")
    assertThat(timeTickDistance.formatAsOffset(ticks[1], I18nConfiguration.US)).isEqualTo("2023")
    assertThat(timeTickDistance.formatAsOffset(ticks[2], I18nConfiguration.US)).isEqualTo("2030")
    assertThat(timeTickDistance.formatAsOffset(ticks[3], I18nConfiguration.US)).isEqualTo("2037")
    assertThat(timeTickDistance.formatAsOffset(ticks[4], I18nConfiguration.US)).isEqualTo("2044")
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceYears12() {
    val timeTickDistance = DistanceYears(12)
    val ticks = timeTickDistance.calculateTicks(time, time + TimeConstants.millisPerYear * 30, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(3)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2016-01-01T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2028-01-01T05:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2040-01-01T05:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)

    assertThat(timeTickDistance.formatAsOffset(ticks[0], I18nConfiguration.US)).isEqualTo("2016")
    assertThat(timeTickDistance.formatAsOffset(ticks[1], I18nConfiguration.US)).isEqualTo("2028")
    assertThat(timeTickDistance.formatAsOffset(ticks[2], I18nConfiguration.US)).isEqualTo("2040")
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceMonths6() {
    val timeTickDistance = DistanceMonths(6)
    val ticks = timeTickDistance.calculateTicks(time, time + TimeConstants.millisPerYear * 2, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(5)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-01T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-07-01T04:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2020-01-01T05:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2020-07-01T04:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)

    assertThat(timeTickDistance.formatAsOffset(ticks[0], I18nConfiguration.US)).isEqualTo("January 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[1], I18nConfiguration.US)).isEqualTo("July 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[2], I18nConfiguration.US)).isEqualTo("January 2020")
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceMonths6_2() {
    @ms val start = time + TimeConstants.millisPerDay * 40
    assertThat(start.formatUtcForDebug()).isEqualTo("2019-02-16T15:45:19.123Z")

    val timeTickDistance = DistanceMonths(6)
    val ticks = timeTickDistance.calculateTicks(start, start + TimeConstants.millisPerYear * 2, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(5)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-01T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-07-01T04:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2020-01-01T05:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2020-07-01T04:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)

    assertThat(timeTickDistance.formatAsOffset(ticks[0], I18nConfiguration.US)).isEqualTo("January 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[1], I18nConfiguration.US)).isEqualTo("July 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[2], I18nConfiguration.US)).isEqualTo("January 2020")
    assertThat(timeTickDistance.formatAsOffset(ticks[3], I18nConfiguration.US)).isEqualTo("July 2020")
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceMonths3() {
    val timeTickDistance = DistanceMonths(3)
    val ticks = timeTickDistance.calculateTicks(time, time + TimeConstants.millisPerYear * 2, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(9)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-01T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-04-01T04:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-07-01T04:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-10-01T04:00:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2020-01-01T05:00:00.000Z")
    assertThat(ticks[5].formatUtcForDebug()).isEqualTo("2020-04-01T04:00:00.000Z")
    assertThat(ticks[6].formatUtcForDebug()).isEqualTo("2020-07-01T04:00:00.000Z")
    assertThat(ticks[7].formatUtcForDebug()).isEqualTo("2020-10-01T04:00:00.000Z")
    assertThat(ticks[8].formatUtcForDebug()).isEqualTo("2021-01-01T05:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)

    assertThat(timeTickDistance.formatAsOffset(ticks[0], I18nConfiguration.US)).isEqualTo("January 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[1], I18nConfiguration.US)).isEqualTo("April 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[2], I18nConfiguration.US)).isEqualTo("July 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[3], I18nConfiguration.US)).isEqualTo("October 2019")
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceMonths2() {
    val timeTickDistance = DistanceMonths(2)
    val ticks = timeTickDistance.calculateTicks(time, time + TimeConstants.millisPerYear * 2, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(13)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-01T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-03-01T05:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-05-01T04:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-07-01T04:00:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2019-09-01T04:00:00.000Z")
    assertThat(ticks[5].formatUtcForDebug()).isEqualTo("2019-11-01T04:00:00.000Z")
    assertThat(ticks[6].formatUtcForDebug()).isEqualTo("2020-01-01T05:00:00.000Z")
    assertThat(ticks[7].formatUtcForDebug()).isEqualTo("2020-03-01T05:00:00.000Z")
    assertThat(ticks[8].formatUtcForDebug()).isEqualTo("2020-05-01T04:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)

    assertThat(timeTickDistance.formatAsOffset(ticks[0], I18nConfiguration.US)).isEqualTo("January 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[1], I18nConfiguration.US)).isEqualTo("March 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[2], I18nConfiguration.US)).isEqualTo("May 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[3], I18nConfiguration.US)).isEqualTo("July 2019")
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceMonths1() {
    val timeTickDistance = DistanceMonths(1)
    val ticks = timeTickDistance.calculateTicks(time, time + TimeConstants.millisPerYear * 1, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(14)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-01T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-02-01T05:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-03-01T05:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-04-01T04:00:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2019-05-01T04:00:00.000Z")
    assertThat(ticks[5].formatUtcForDebug()).isEqualTo("2019-06-01T04:00:00.000Z")
    assertThat(ticks[6].formatUtcForDebug()).isEqualTo("2019-07-01T04:00:00.000Z")
    assertThat(ticks[7].formatUtcForDebug()).isEqualTo("2019-08-01T04:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)

    assertThat(timeTickDistance.formatAsOffset(ticks[0], I18nConfiguration.US)).isEqualTo("January 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[1], I18nConfiguration.US)).isEqualTo("February 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[2], I18nConfiguration.US)).isEqualTo("March 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[3], I18nConfiguration.US)).isEqualTo("April 2019")
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceDays15() {
    val end = time + TimeConstants.millisPerDay * 60
    assertThat(end.formatUtcForDebug()).isEqualTo("2019-03-08T15:45:19.123Z")

    val timeTickDistance = DistanceDays(DistanceDays.TicksPerMonth.Every15Days)
    val ticks = timeTickDistance.calculateTicks(time, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(6)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-01T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-01-15T05:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-02-01T05:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-02-15T05:00:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2019-03-01T05:00:00.000Z")
    assertThat(ticks[5].formatUtcForDebug()).isEqualTo("2019-03-15T04:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)

    assertThat(timeTickDistance.formatAsOffset(ticks[0], I18nConfiguration.US)).isEqualTo("Jan 1, 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[1], I18nConfiguration.US)).isEqualTo("Jan 15, 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[2], I18nConfiguration.US)).isEqualTo("Feb 1, 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[3], I18nConfiguration.US)).isEqualTo("Feb 15, 2019")
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceDays5() {
    assertThat(time.formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.123Z")

    val end = time + TimeConstants.millisPerDay * 30
    assertThat(end.formatUtcForDebug()).isEqualTo("2019-02-06T15:45:19.123Z")

    val timeTickDistance = DistanceDays(DistanceDays.TicksPerMonth.Every5Days)
    val ticks = timeTickDistance.calculateTicks(time, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(8)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-05T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-01-10T05:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-01-15T05:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-01-20T05:00:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2019-01-25T05:00:00.000Z")
    assertThat(ticks[5].formatUtcForDebug()).isEqualTo("2019-02-01T05:00:00.000Z")
    assertThat(ticks[6].formatUtcForDebug()).isEqualTo("2019-02-05T05:00:00.000Z")
    assertThat(ticks[7].formatUtcForDebug()).isEqualTo("2019-02-10T05:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)

    assertThat(timeTickDistance.formatAsOffset(ticks[0], I18nConfiguration.US)).isEqualTo("Jan 5, 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[1], I18nConfiguration.US)).isEqualTo("Jan 10, 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[2], I18nConfiguration.US)).isEqualTo("Jan 15, 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[3], I18nConfiguration.US)).isEqualTo("Jan 20, 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[4], I18nConfiguration.US)).isEqualTo("Jan 25, 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[5], I18nConfiguration.US)).isEqualTo("Feb 1, 2019")
    assertThat(timeTickDistance.formatAsOffset(ticks[6], I18nConfiguration.US)).isEqualTo("Feb 5, 2019")
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceDays1() {
    val start = 1.7040593889296738E12
    assertThat(start.formatUtcForDebug()).isEqualTo("2023-12-31T21:49:48.929Z")
    val end = 1.704079753604972E12
    assertThat(end.formatUtcForDebug()).isEqualTo("2024-01-01T03:29:13.604Z")

    val timeTickDistance = DistanceDays(DistanceDays.TicksPerMonth.EveryDay)
    val ticks = timeTickDistance.calculateTicks(start, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(2)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2023-12-31T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2024-01-01T05:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)

    assertThat(timeTickDistance.formatAsOffset(ticks[0], I18nConfiguration.US)).isEqualTo("Dec 31, 2023")
    assertThat(timeTickDistance.formatAsOffset(ticks[1], I18nConfiguration.US)).isEqualTo("Jan 1, 2024")
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceDays1_2() {
    val start = 1.7040593889296738E12
    assertThat(start.formatUtcForDebug()).isEqualTo("2023-12-31T21:49:48.929Z")
    val end = start + TimeConstants.millisPerDay * 7
    assertThat(end.formatUtcForDebug()).isEqualTo("2024-01-07T21:49:48.929Z")

    val timeTickDistance = DistanceDays(DistanceDays.TicksPerMonth.EveryDay)
    val ticks = timeTickDistance.calculateTicks(start, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(9)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2023-12-31T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2024-01-01T05:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2024-01-02T05:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2024-01-03T05:00:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2024-01-04T05:00:00.000Z")
    assertThat(ticks[5].formatUtcForDebug()).isEqualTo("2024-01-05T05:00:00.000Z")
    assertThat(ticks[6].formatUtcForDebug()).isEqualTo("2024-01-06T05:00:00.000Z")
    assertThat(ticks[7].formatUtcForDebug()).isEqualTo("2024-01-07T05:00:00.000Z")
    assertThat(ticks[8].formatUtcForDebug()).isEqualTo("2024-01-08T05:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceDays1_YearOverlap() {
    val start = 1.7038363819296738E12
    assertThat(start.formatUtcForDebug()).isEqualTo("2023-12-29T07:53:01.929Z")
    val end = start + TimeConstants.millisPerDay * 7
    assertThat(end.formatUtcForDebug()).isEqualTo("2024-01-05T07:53:01.929Z")

    val timeTickDistance = DistanceDays(DistanceDays.TicksPerMonth.EveryDay)
    val ticks = timeTickDistance.calculateTicks(start, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(9)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2023-12-29T05:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2023-12-30T05:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2023-12-31T05:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2024-01-01T05:00:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2024-01-02T05:00:00.000Z")
    assertThat(ticks[5].formatUtcForDebug()).isEqualTo("2024-01-03T05:00:00.000Z")
    assertThat(ticks[6].formatUtcForDebug()).isEqualTo("2024-01-04T05:00:00.000Z")
    assertThat(ticks[7].formatUtcForDebug()).isEqualTo("2024-01-05T05:00:00.000Z")
    assertThat(ticks[8].formatUtcForDebug()).isEqualTo("2024-01-06T05:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceHours6() {
    val end = time + TimeConstants.millisPerHour * 40
    assertThat(end.formatUtcForDebug()).isEqualTo("2019-01-09T07:45:19.123Z")

    val timeTickDistance = DistanceHours(6)
    val ticks = timeTickDistance.calculateTicks(time, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(7)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-07T15:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-01-07T21:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-01-08T03:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-01-08T09:00:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2019-01-08T15:00:00.000Z")
    assertThat(ticks[5].formatUtcForDebug()).isEqualTo("2019-01-08T21:00:00.000Z")
    assertThat(ticks[6].formatUtcForDebug()).isEqualTo("2019-01-09T03:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceHours4() {
    val end = time + TimeConstants.millisPerHour * 30
    assertThat(end.formatUtcForDebug()).isEqualTo("2019-01-08T21:45:19.123Z")

    val timeTickDistance = DistanceHours(4)
    val ticks = timeTickDistance.calculateTicks(time, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(8)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-07T15:00:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-01-07T19:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-01-07T23:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-01-08T03:00:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2019-01-08T07:00:00.000Z")
    assertThat(ticks[5].formatUtcForDebug()).isEqualTo("2019-01-08T11:00:00.000Z")
    assertThat(ticks[6].formatUtcForDebug()).isEqualTo("2019-01-08T15:00:00.000Z")
    assertThat(ticks[7].formatUtcForDebug()).isEqualTo("2019-01-08T19:00:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceMinutes10() {
    val end = time + TimeConstants.millisPerHour * 1.2
    assertThat(end.formatUtcForDebug()).isEqualTo("2019-01-07T16:57:19.123Z")

    val timeTickDistance = DistanceMinutes(10)
    val ticks = timeTickDistance.calculateTicks(time, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(8)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-07T15:40:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-01-07T15:50:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-01-07T16:00:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-01-07T16:10:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2019-01-07T16:20:00.000Z")
    assertThat(ticks[5].formatUtcForDebug()).isEqualTo("2019-01-07T16:30:00.000Z")
    assertThat(ticks[6].formatUtcForDebug()).isEqualTo("2019-01-07T16:40:00.000Z")
    assertThat(ticks[7].formatUtcForDebug()).isEqualTo("2019-01-07T16:50:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceMinutes15() {
    val end = time + TimeConstants.millisPerHour * 1.2
    assertThat(end.formatUtcForDebug()).isEqualTo("2019-01-07T16:57:19.123Z")

    val timeTickDistance = DistanceMinutes(15)

    val ticks = timeTickDistance.calculateTicks(time, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(5)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-01-07T16:00:00.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-01-07T16:15:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-01-07T16:30:00.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2019-01-07T16:45:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceSeconds30() {
    val end = time + TimeConstants.millisPerMinute * 3
    assertThat(end.formatUtcForDebug()).isEqualTo("2019-01-07T15:48:19.123Z")

    val timeTickDistance = DistanceSeconds(30)
    val ticks = timeTickDistance.calculateTicks(time, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(7)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:00.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:30.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-01-07T15:46:00.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-01-07T15:46:30.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2019-01-07T15:47:00.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceSeconds10() {
    val end = time + TimeConstants.millisPerMinute * 1
    assertThat(end.formatUtcForDebug()).isEqualTo("2019-01-07T15:46:19.123Z")

    val timeTickDistance = DistanceSeconds(10)
    val ticks = timeTickDistance.calculateTicks(time, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(7)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:10.000Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:20.000Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:30.000Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:40.000Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:50.000Z")

    verifyGlobalIndices(timeTickDistance, ticks)
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testDistanceMillis() {
    assertThat(time.formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.123Z")
    val end = time + TimeConstants.millisPerSecond * 1
    assertThat(end.formatUtcForDebug()).isEqualTo("2019-01-07T15:45:20.123Z")

    val timeTickDistance = DistanceMillis(100.0)
    val ticks = timeTickDistance.calculateTicks(time, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(11)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.100Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.200Z")
    assertThat(ticks[2].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.300Z")
    assertThat(ticks[3].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.400Z")
    assertThat(ticks[4].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.500Z")
    assertThat(ticks[5].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.600Z")
    assertThat(ticks[6].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.700Z")
    assertThat(ticks[7].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.800Z")
    assertThat(ticks[8].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:19.900Z")
    assertThat(ticks[9].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:20.000Z")
    assertThat(ticks[10].formatUtcForDebug()).isEqualTo("2019-01-07T15:45:20.100Z")

    verifyGlobalIndices(timeTickDistance, ticks)
  }

  @WithTimeZone("America/New_York")
  @Test
  fun testVerySmall() {
    val start = 1.7040697786616245E12
    assertThat(start.formatUtcForDebug()).isEqualTo("2024-01-01T00:42:58.661Z")
    val end = 1.7040697786624827E12
    assertThat(end.formatUtcForDebug()).isEqualTo("2024-01-01T00:42:58.662Z")

    val ticks = DistanceMillis(1.0).calculateTicks(start, end, TimeZone.NewYork)
    assertThat(ticks.size).isEqualTo(2)
    assertThat(ticks[0].formatUtcForDebug()).isEqualTo("2024-01-01T00:42:58.661Z")
    assertThat(ticks[1].formatUtcForDebug()).isEqualTo("2024-01-01T00:42:58.662Z")
  }

  @Test
  fun testOffset2ticks() {
    DistanceYears(1).smallestPossibleTickDistance().let {
      assertThat(it).isInstanceOf(DistanceDays::class)
    }
    DistanceMonths(1).smallestPossibleTickDistance().let {
      assertThat(it).isInstanceOf(DistanceDays::class)
    }
  }


  @Test
  fun testCoerceAtLeast() {
    DistanceMonths(1).coerceAtLeast(DistanceMillis(123.0)).let {
      assertThat(it).isInstanceOf(DistanceMonths::class)
    }
  }

  @Test
  fun testRoundDownMonth() {
    assertThat(1.roundDownToInt(6)).isEqualTo(1)
    assertThat(2.roundDownToInt(6)).isEqualTo(1)
    assertThat(3.roundDownToInt(6)).isEqualTo(1)
    assertThat(4.roundDownToInt(6)).isEqualTo(1)
    assertThat(5.roundDownToInt(6)).isEqualTo(1)
    assertThat(6.roundDownToInt(6)).isEqualTo(1)
    assertThat(7.roundDownToInt(6)).isEqualTo(7)
    assertThat(8.roundDownToInt(6)).isEqualTo(7)
    assertThat(9.roundDownToInt(6)).isEqualTo(7)
    assertThat(10.roundDownToInt(6)).isEqualTo(7)
    assertThat(11.roundDownToInt(6)).isEqualTo(7)
    assertThat(12.roundDownToInt(6)).isEqualTo(7)
  }

  private fun Int.roundDownToInt(base: Int): Int {
    return (this - 1).roundDownToBase(base) + 1
  }
}
