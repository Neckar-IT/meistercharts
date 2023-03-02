package com.meistercharts.algorithms.axis

import assertk.*
import assertk.assertions.*
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.TimeZone
import it.neckar.open.time.toDoubleMillis
import com.soywiz.klock.DateTime
import org.junit.jupiter.api.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class TickDistanceBugTest {
  @Test
  fun testIAE() {
    val startDate = ZonedDateTime.of(2022, 8, 5, 12, 19, 59, TimeUnit.MILLISECONDS.toNanos(999L).toInt(), ZoneOffset.UTC)
    val endDate = ZonedDateTime.of(2022, 8, 5, 12, 22, 49, TimeUnit.MILLISECONDS.toNanos(349L).toInt(), ZoneOffset.UTC)

    val ticks = DistanceSeconds(10).calculateTicks(startDate.toDoubleMillis(), endDate.toDoubleMillis(), TimeZone.Berlin, true)

    assertThat(ticks.size).isGreaterThan(2)
    assertThat(ticks.first().formatUtc()).isEqualTo("2022-08-05T12:20:00.000")
  }

  @Test
  fun testReproduce() {
    val startDate = ZonedDateTime.of(2022, 8, 5, 12, 20, 0, 0, ZoneOffset.UTC).minusNanos(123)
    val endDate = ZonedDateTime.of(2022, 8, 5, 12, 22, 49, TimeUnit.MILLISECONDS.toNanos(349L).toInt(), ZoneOffset.UTC)

    val startMillis = startDate.toDoubleMillis()
    assertThat(startMillis.formatUtc()).isEqualTo("2022-08-05T12:19:59.999")

    val startKlock = DateTime(startMillis).utc2DateTimeTz(TimeZone.Berlin)
    assertThat(startKlock.milliseconds).isEqualTo(0)
    assertThat(startKlock.seconds).isEqualTo(0)


    val ticks = DistanceSeconds(10).calculateTicks(startMillis, endDate.toDoubleMillis(), TimeZone.Berlin, true)
    assertThat(ticks.first().formatUtc()).isEqualTo("2022-08-05T12:20:00.000")
  }
}

