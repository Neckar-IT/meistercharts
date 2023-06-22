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
package com.meistercharts.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.time.VirtualNowProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.formatting.dateTimeFormatIso8601
import it.neckar.open.formatting.formatUtc
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.Locale
import it.neckar.open.test.utils.VirtualTime
import it.neckar.open.time.TimeZone
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
