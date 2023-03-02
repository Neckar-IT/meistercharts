import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.time.DefaultTimeZoneOffsetProvider
import it.neckar.open.time.TimeZone
import kotlin.test.Test

/**
 *
 */
class TimeZoneOffsetProviderTest {
  @Test
  fun testFragmentsOfMillis() {
    assertThat(DefaultTimeZoneOffsetProvider().timeZoneOffset(1000000.5, TimeZone.Berlin)).isEqualTo(-3600000)

    assertThat(DefaultTimeZoneOffsetProvider().timeZoneOffset(1000000.9, TimeZone.Berlin)).isEqualTo(-3600000)
    assertThat(DefaultTimeZoneOffsetProvider().timeZoneOffset(1000000.1, TimeZone.Berlin)).isEqualTo(-3600000)
    assertThat(DefaultTimeZoneOffsetProvider().timeZoneOffset(1000000.0, TimeZone.Berlin)).isEqualTo(-3600000)

  }
}
