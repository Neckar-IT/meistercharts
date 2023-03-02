package com.meistercharts.algorithms.axis

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.TimeZone
import it.neckar.open.unit.si.ms
import com.soywiz.klock.DateTime
import org.junit.jupiter.api.Test

/**
 *
 */
class KlockExtensionsKtTest {
  @Test
  fun testIt() {
    val now: @ms Double = 1.5900732415E12
    assertThat(now.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")

    val dateTimeTz = DateTime(now).utc2DateTimeTz(TimeZone.Berlin)

    assertThat(dateTimeTz.hours).isEqualTo(17)
    assertThat(dateTimeTz.minutes).isEqualTo(0)
    assertThat(dateTimeTz.seconds).isEqualTo(41)
    assertThat(dateTimeTz.minutesOfDay).isEqualTo(17 * 60)
    assertThat(dateTimeTz.secondsOfDay).isEqualTo(17 * 60 * 60 + 41)
  }
}
