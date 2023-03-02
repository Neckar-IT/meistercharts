package it.neckar.open.time

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.time.DefaultCachedTimeZoneOffsetProvider
import com.meistercharts.algorithms.time.DefaultTimeZoneOffsetProvider
import it.neckar.open.formatting.formatUtc
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit


class TimeZoneOffsetProviderTest {

  @Test
  fun testOffsets() {
    val timeZoneOffsetProvider = DefaultTimeZoneOffsetProvider()
    assertThat(timeZoneOffsetProvider.timeZoneOffset(1607867526127.0, TimeZone("Europe/Berlin"))).isEqualTo(1.hours.toDouble(DurationUnit.MILLISECONDS))
    assertThat(timeZoneOffsetProvider.timeZoneOffset(1607867526127.0, TimeZone("Africa/Asmara"))).isEqualTo(3.hours.toDouble(DurationUnit.MILLISECONDS))
    assertThat(timeZoneOffsetProvider.timeZoneOffset(1607867526127.0, TimeZone("America/Aruba"))).isEqualTo(-4.hours.toDouble(DurationUnit.MILLISECONDS))
    assertThat(timeZoneOffsetProvider.timeZoneOffset(1607867526127.0, TimeZone("Indian/Christmas"))).isEqualTo(7.hours.toDouble(DurationUnit.MILLISECONDS))
    assertThat(timeZoneOffsetProvider.timeZoneOffset(1607867526127.0, TimeZone("Pacific/Guadalcanal"))).isEqualTo(11.hours.toDouble(DurationUnit.MILLISECONDS))
    assertThat(timeZoneOffsetProvider.timeZoneOffset(1607867526127.0, TimeZone("Africa/Algiers"))).isEqualTo(1.hours.toDouble(DurationUnit.MILLISECONDS))
    assertThat(timeZoneOffsetProvider.timeZoneOffset(1607867526127.0, TimeZone("America/Adak"))).isEqualTo(-10.hours.toDouble(DurationUnit.MILLISECONDS))
    assertThat(timeZoneOffsetProvider.timeZoneOffset(1607867526127.0, TimeZone("Europe/Tallinn"))).isEqualTo(2.hours.toDouble(DurationUnit.MILLISECONDS))
  }

  @Test
  fun testTimestampAlignment() {
    assertThat(1607864400000.0.formatUtc()).isEqualTo("2020-12-13T13:00:00.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607864400000.0).formatUtc()).isEqualTo("2020-12-13T13:00:00.000")

    assertThat(1607864401000.0.formatUtc()).isEqualTo("2020-12-13T13:00:01.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607864401000.0).formatUtc()).isEqualTo("2020-12-13T13:00:00.000")

    assertThat(1607865299000.0.formatUtc()).isEqualTo("2020-12-13T13:14:59.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607864401000.0).formatUtc()).isEqualTo("2020-12-13T13:00:00.000")

    assertThat(1607865300000.0.formatUtc()).isEqualTo("2020-12-13T13:15:00.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607865300000.0).formatUtc()).isEqualTo("2020-12-13T13:15:00.000")

    assertThat(1607865301000.0.formatUtc()).isEqualTo("2020-12-13T13:15:01.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607865301000.0).formatUtc()).isEqualTo("2020-12-13T13:15:00.000")

    assertThat(1607866199000.0.formatUtc()).isEqualTo("2020-12-13T13:29:59.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607865301000.0).formatUtc()).isEqualTo("2020-12-13T13:15:00.000")

    assertThat(1607866200000.0.formatUtc()).isEqualTo("2020-12-13T13:30:00.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607866200000.0).formatUtc()).isEqualTo("2020-12-13T13:30:00.000")

    assertThat(1607866201000.0.formatUtc()).isEqualTo("2020-12-13T13:30:01.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607866201000.0).formatUtc()).isEqualTo("2020-12-13T13:30:00.000")

    assertThat(1607867099000.0.formatUtc()).isEqualTo("2020-12-13T13:44:59.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607866201000.0).formatUtc()).isEqualTo("2020-12-13T13:30:00.000")

    assertThat(1607867100000.0.formatUtc()).isEqualTo("2020-12-13T13:45:00.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607867100000.0).formatUtc()).isEqualTo("2020-12-13T13:45:00.000")

    assertThat(1607867101000.0.formatUtc()).isEqualTo("2020-12-13T13:45:01.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607867101000.0).formatUtc()).isEqualTo("2020-12-13T13:45:00.000")

    assertThat(1607867999000.0.formatUtc()).isEqualTo("2020-12-13T13:59:59.000")
    assertThat(DefaultCachedTimeZoneOffsetProvider.alignTimestampToTimeZones(1607867101000.0).formatUtc()).isEqualTo("2020-12-13T13:45:00.000")
  }
}
