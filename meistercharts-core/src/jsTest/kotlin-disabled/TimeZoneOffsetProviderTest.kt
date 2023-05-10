/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
