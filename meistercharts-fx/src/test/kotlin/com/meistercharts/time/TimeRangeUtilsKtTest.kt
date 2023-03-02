package com.meistercharts.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.time.millis2Instant
import it.neckar.open.time.timeRangeTo
import it.neckar.open.time.toDoubleMillis
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.absoluteValue

internal class TimeRangeUtilsKtTest {
  @Test
  internal fun testConversionToInstant() {
    val millis = 1312003987_123.987548828125
    val instant = millis2Instant(millis)

    assertThat(instant.toEpochMilli()).isEqualTo(1312003987_123L)
    assertThat(instant.nano).isEqualTo(123_987548)
  }

  @Test
  internal fun testToTimeRange() {
    val from = Instant.ofEpochSecond(1567325402, 123_000_000) //maximum precision that is reachable
    val timeRange = from.timeRangeTo(from.plusMillis(1000))

    assertThat(timeRange.span).isEqualTo(1_000.0)
  }

  @Test
  fun testInstant() {
    val instant = Instant.ofEpochSecond(1567325402, 123_000_000) //maximum precision that is reachable
    assertThat(ZonedDateTime.ofInstant(instant, europe_berlin).toString()).isEqualTo("2019-09-01T10:10:02.123+02:00[Europe/Berlin]")

    assertThat(instant.toEpochMilli()).isEqualTo(1567325402_123L)
    assertThat(instant.nano).isEqualTo(123_000_000)
    assertThat(instant.epochSecond).isEqualTo(1567_325_402L)
    assertThat(instant.toDoubleMillis()).isEqualTo(1567325402_123.0)

    val backInstant = millis2Instant(instant.toDoubleMillis())
    assertThat(backInstant).isEqualTo(instant)

    assertThat(backInstant.nano).isEqualTo(instant.nano)
    assertThat(backInstant.toEpochMilli()).isEqualTo(instant.toEpochMilli())
  }

  @Test
  internal fun testOtherValues() {
    checkRoundTrip(Instant.ofEpochSecond(1567325402, 500_000_000))
    checkRoundTrip(Instant.ofEpochSecond(1567325402, 550_000_000))
    checkRoundTrip(Instant.ofEpochSecond(1567325402, 555_000_000))
    checkRoundTrip(Instant.ofEpochSecond(1567325402, 555_600_000))
    checkRoundTrip(Instant.ofEpochSecond(1567325402, 555_670_000))
    checkRoundTrip(Instant.ofEpochSecond(1567325402, 555_678_000))
    checkRoundTrip(Instant.ofEpochSecond(1567325402, 555_678_900))
    checkRoundTrip(Instant.ofEpochSecond(1567325402, 555_678_910))
    checkRoundTrip(Instant.ofEpochSecond(1567325402, 555_678_912))
  }

  @Test
  internal fun testPrecision() {
    ensureErrorSmallerThan(ZonedDateTime.of(2018, 8, 3, 7, 35, 14, 987_654_321, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(2000, 8, 3, 7, 35, 14, 987_654_321, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(1950, 8, 3, 7, 35, 14, 987_654_321, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(2020, 8, 3, 7, 35, 14, 987_654_321, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(2030, 8, 3, 7, 35, 14, 987_654_321, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(2040, 8, 3, 7, 35, 14, 987_654_321, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(2100, 8, 3, 7, 35, 14, 987_654_321, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(2030, 8, 3, 7, 35, 14, 987_654_399, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(2030, 8, 3, 7, 35, 14, 987_654_999, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(2030, 8, 3, 7, 35, 14, 987_654_599, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(2030, 8, 3, 7, 35, 14, 987_654_199, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(2030, 8, 3, 7, 35, 14, 987_654_799, europe_berlin).toInstant(), 1_000)
    ensureErrorSmallerThan(ZonedDateTime.of(1700, 8, 3, 7, 35, 14, 987_654_799, europe_berlin).toInstant(), 1_000)

    ensureErrorSmallerThan(ZonedDateTime.of(1400, 8, 3, 7, 35, 14, 987_654_799, europe_berlin).toInstant(), 10_000)
    ensureErrorSmallerThan(ZonedDateTime.of(100, 8, 3, 7, 35, 14, 987_654_799, europe_berlin).toInstant(), 10_000)
    ensureErrorSmallerThan(ZonedDateTime.of(-1000, 8, 3, 7, 35, 14, 987_654_799, europe_berlin).toInstant(), 10_000)
    ensureErrorSmallerThan(ZonedDateTime.of(4000, 8, 3, 7, 35, 14, 987_654_799, europe_berlin).toInstant(), 10_000)
  }

  private fun ensureErrorSmallerThan(instant: Instant, toleranceNanos: Int): Delta {
    val error = calculateError(instant)
    assertThat(error.deltaSeconds, "Error $error for $instant").isEqualTo(0)
    assertThat(error.deltaNanos, "Error $error for $instant").isLessThan(toleranceNanos)
    return error
  }

  /**
   * Calculates the conversion error for the given instant
   */
  private fun calculateError(instant: Instant): Delta {
    @ms val millis = instant.toDoubleMillis()
    val convertedBack = millis2Instant(millis)

    val deltaSeconds = instant.epochSecond - convertedBack.epochSecond
    val deltaNanos = instant.nano - convertedBack.nano

    return Delta(deltaSeconds.absoluteValue, deltaNanos.absoluteValue)
  }

  private fun checkRoundTrip(instant: Instant) {
    @ms val millis = instant.toDoubleMillis()

    val convertedBack = millis2Instant(millis)

    assertThat(convertedBack.toEpochMilli(), ZonedDateTime.ofInstant(instant, europe_berlin).toString()).isEqualTo(instant.toEpochMilli())
    assertThat(convertedBack.nano, ZonedDateTime.ofInstant(instant, europe_berlin).toString()).isBetween(instant.nano - 100, instant.nano + 100)
  }
}

/**
 * The zone id for "Europe/Berlin"
 */
val europe_berlin: ZoneId = ZoneId.of("Europe/Berlin")

data class Delta(val deltaSeconds: Long, val deltaNanos: Int)
