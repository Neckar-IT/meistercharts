package it.neckar.open.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.time.VirtualNowProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.formatting.dateTimeFormatIso8601
import it.neckar.open.formatting.formatUtc
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.Locale
import it.neckar.open.test.utils.VirtualTime
import org.junit.jupiter.api.Test

/**
 *
 */
@VirtualTime
class VirtualNowProviderTest {
  @Test
  fun testNow() {
    assertThat(nowMillis()).isEqualTo(1.6168815230025E12)
    assertThat(nowMillis().formatUtc()).isEqualTo("2021-03-27T21:45:23.002")
    assertThat(dateTimeFormatIso8601.format(nowMillis(), I18nConfiguration.Germany)).isEqualTo("2021-03-27T22:45:23.002+01:00[Europe/Berlin]")
    assertThat(dateTimeFormatIso8601.format(nowMillis(), I18nConfiguration(TimeZone.Tokyo, Locale.Germany))).isEqualTo("2021-03-28T06:45:23.002+09:00[Asia/Tokyo]")
  }

  @Test
  fun testPlus(virtualNowProvider: VirtualNowProvider) {
    assertThat(virtualNowProvider.nowMillis()).isEqualTo(VirtualTime.defaultNow)

    virtualNowProvider.add(100.0)
    assertThat(virtualNowProvider.nowMillis()).isEqualTo(VirtualTime.defaultNow + 100.0)

    virtualNowProvider.add(0.0)
    assertThat(virtualNowProvider.nowMillis()).isEqualTo(VirtualTime.defaultNow + 100.0)
  }
}
