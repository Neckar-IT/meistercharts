package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.ZoneOffset


class WeekInEpochTest {
  @Test
  fun testToOffsetDateTime() {
    val offsetDateTime = OffsetDateTime.of(2015, 11, 18, 0, 0, 0, 0, ZoneOffset.UTC)
    val weekInEpoch = WeekInEpoch.calculate(offsetDateTime)
    assertThat(weekInEpoch.asOffsetDateTime()).isEqualTo(OffsetDateTime.of(2015, 11, 16, 0, 0, 0, 0, ZoneOffset.UTC))
  }

  @Test
  fun testDayOfWeek() {
    assertThat(WeekInEpoch.base.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
  }

  @Test
  fun testBegin() {
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(0) //THURSDAY
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(1970, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(0) //Friday
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(1970, 1, 3, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(0) //Sat
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(1970, 1, 4, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(0) //Sun
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(1970, 1, 5, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(1) //Mon
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(1970, 1, 6, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(1)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(1970, 1, 7, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(1)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(1970, 1, 8, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(1)
  }

  @Test
  fun testCalculation() {
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 18, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(2394)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 19, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(2394)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 20, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(2394)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 21, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(2394)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 22, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(2394)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 23, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(2395) //Monday
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 24, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(2395)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(2395)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 26, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(2395)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 27, 0, 0, 0, 0, ZoneOffset.UTC)).week).isEqualTo(2395)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 23, 0, 0, 0, 0, ZoneOffset.UTC).minusSeconds(1)).week).isEqualTo(2394)
    assertThat(WeekInEpoch.calculate(OffsetDateTime.of(2015, 11, 23, 0, 0, 0, 0, ZoneOffset.UTC).plusSeconds(1)).week).isEqualTo(2395)
  }

  @Test
  fun testStartOfWeek() {
    assertThat(WeekInEpoch(0).startOfWeek.toString()).isEqualTo("1969-12-29")
    assertThat(WeekInEpoch(1).startOfWeek.toString()).isEqualTo("1970-01-05")
    assertThat(WeekInEpoch(2394).startOfWeek.toString()).isEqualTo("2015-11-16")
  }

  @Test
  fun testFormat() {
    assertThat(WeekInEpoch(2394).formatWithDate()).isEqualTo("2394_2015-11-16")
    assertThat(WeekInEpoch(0).formatWithDate()).isEqualTo("0000_1969-12-29")
  }
}
