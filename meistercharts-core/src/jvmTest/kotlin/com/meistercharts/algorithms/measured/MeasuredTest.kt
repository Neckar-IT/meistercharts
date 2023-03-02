package com.meistercharts.algorithms.measured

import assertk.*
import assertk.assertions.*
import io.nacular.measured.units.Length.Companion.meters
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.Time.Companion.minutes
import io.nacular.measured.units.Time.Companion.seconds
import io.nacular.measured.units.div
import io.nacular.measured.units.times
import org.junit.jupiter.api.Test

/**
 *
 */
class MeasuredTest {
  @Test
  fun testIt() {
    val duration: Measure<Time> = 1_700.0 * milliseconds

    assertThat(duration.amount).isEqualTo(1700.0)
    assertThat(duration.units.suffix).isEqualTo("ms")
  }

  @Test
  fun testComplex() {
    val velocity = 5 * meters / seconds
    val acceleration = 9 * meters / (seconds * seconds)
    val time = 1 * minutes
  }
}
