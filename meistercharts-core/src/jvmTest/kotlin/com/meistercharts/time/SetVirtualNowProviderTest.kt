package it.neckar.open.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.time.ClockNowProvider
import it.neckar.open.time.VirtualNowProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.time.nowProvider
import it.neckar.open.time.resetNowProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

/**
 *
 */
class SetVirtualNowProviderTest {
  @AfterEach
  fun tearDown() {
    resetNowProvider()
    assertThat(nowProvider).isSameAs(ClockNowProvider)
  }

  @Test
  fun testNow() {
    assertThat(nowProvider).isSameAs(ClockNowProvider)

    val fixedNowProvider = VirtualNowProvider(50000.0)

    assertThat(nowMillis()).isGreaterThan(50000.0)
    nowProvider = fixedNowProvider
    assertThat(nowMillis()).isEqualTo(50000.0)
  }
}
