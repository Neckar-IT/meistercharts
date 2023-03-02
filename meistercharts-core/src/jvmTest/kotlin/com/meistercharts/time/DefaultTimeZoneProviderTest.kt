package it.neckar.open.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.i18n.SystemTimeZoneProvider
import it.neckar.open.test.utils.WithTimeZone
import org.junit.jupiter.api.Test

class DefaultTimeZoneProviderTest {
  @WithTimeZone("Europe/Berlin")
  @Test
  internal fun testBerlin() {
    assertThat(SystemTimeZoneProvider().systemTimeZone).isEqualTo(TimeZone(("Europe/Berlin")))
  }

  @WithTimeZone("Asia/Tokyo")
  @Test
  internal fun testIt() {
    assertThat(SystemTimeZoneProvider().systemTimeZone).isEqualTo(TimeZone(("Asia/Tokyo")))
  }
}
