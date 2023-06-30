package com.meistercharts.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.VirtualNowProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Test

class VirtualNowForDemosTest {
  @Test
  fun testIt() {
    @ms val fixedStart = 17788887777777.0
    assertThat(fixedStart.formatUtc()).isEqualTo("2533-09-15T21:42:57.777")

    @ms val realNow = nowMillis()
    @ms val deltaToReal = fixedStart - realNow

    val virtualNowProvider = VirtualNowProvider(fixedStart)

    assertThat(virtualNowProvider.nowMillis()).isEqualTo(fixedStart)



  }
}
