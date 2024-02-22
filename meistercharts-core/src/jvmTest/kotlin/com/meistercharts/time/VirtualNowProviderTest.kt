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
import it.neckar.datetime.minimal.TimeZone
import it.neckar.open.formatting.dateTimeFormatIso8601
import it.neckar.open.formatting.formatUtc
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.Locale
import it.neckar.open.kotlin.lang.abs
import it.neckar.open.test.utils.VirtualTime
import it.neckar.open.time.ClockNowProvider
import it.neckar.open.time.RealClockTime
import it.neckar.open.time.VirtualNowProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days

/**
 *
 */
@VirtualTime
class VirtualNowProviderTest {
  @Test
  fun testUpdateWithInitial10000() {
    @RealClockTime @ms val now = ClockNowProvider.nowMillis()
    val virtualNowProvider = VirtualNowProvider(10_000.0)

    assertThat(virtualNowProvider.virtualNow).isEqualTo(10_000.0)
    assertThat(virtualNowProvider.offsetBetweenStartedRealTimeAndInitialNow)
      .isGreaterThan(1000.days.inWholeMilliseconds.toDouble()) //should always be correct, since the reference date is in the past

    assertThat(virtualNowProvider.startedRealTime - virtualNowProvider.offsetBetweenStartedRealTimeAndInitialNow).isEqualTo(10_000.0)

    assertThat(virtualNowProvider.startedRealTime).isEqualTo(now)
    assertThat(virtualNowProvider.initialNow).isEqualTo(10_000.0)

    //These checks depend on the real time clock. Therefore, they are prone to fail.
    if (false) {
      assertThat(virtualNowProvider.virtualNow).isEqualTo(10_000.0)
      virtualNowProvider.updateVirtualNow()
      assertThat(virtualNowProvider.virtualNow).isCloseTo(10_000.0, 100.0)
      Thread.sleep(10)
      virtualNowProvider.updateVirtualNow()
      assertThat(virtualNowProvider.virtualNow).isCloseTo(10_000.0, 100.0)
      Thread.sleep(10)
      virtualNowProvider.updateVirtualNow()
      assertThat(virtualNowProvider.virtualNow).isCloseTo(10_000.0, 100.0)
      Thread.sleep(10)
      virtualNowProvider.updateVirtualNow()
      assertThat(virtualNowProvider.virtualNow).isCloseTo(10_000.0, 100.0)
    }
  }

  @Test
  fun testUpdateWithInitialNow() {
    @RealClockTime @ms val now = ClockNowProvider.nowMillis()
    val virtualNowProvider = VirtualNowProvider(now)

    assertThat(virtualNowProvider.virtualNow).isEqualTo(now)
    assertThat(virtualNowProvider.offsetBetweenStartedRealTimeAndInitialNow).isCloseTo(0.0, 100.0)
    assertThat(virtualNowProvider.startedRealTime).isCloseTo(now, 10_000.0)
    assertThat(virtualNowProvider.initialNow).isEqualTo(now)
    assertThat(virtualNowProvider.initialNow).isEqualTo(now)


    //These checks depend on the real time clock. Therefore, they are prone to fail.
    if (false) {
      virtualNowProvider.updateVirtualNow()
      Thread.sleep(10)
      virtualNowProvider.updateVirtualNow()
      Thread.sleep(10)
      virtualNowProvider.updateVirtualNow()
      Thread.sleep(10)
      virtualNowProvider.updateVirtualNow()

      virtualNowProvider.nowMillis().let { virtualNow ->
        val deltaToRealTime = virtualNow - now
        if (deltaToRealTime.abs() > 10_000) {
          println("deltaToRealTime = $deltaToRealTime")
          println("it = ${virtualNow.formatUtc()}")
          println("now = ${now.formatUtc()}")
        }

        assertThat(virtualNow, virtualNow.formatUtc()).isCloseTo(now, 10_000.0)
      }
    }
  }

  @Test
  fun testNow() {
    assertThat(nowMillis()).isEqualTo(1.6168815230025E12)
    assertThat(nowMillis().formatUtc()).isEqualTo("2021-03-27T21:45:23.002Z")
    assertThat(dateTimeFormatIso8601.format(nowMillis(), I18nConfiguration.Germany)).isEqualTo("2021-03-27T22:45:23.002+01:00[Europe/Berlin]")
    assertThat(dateTimeFormatIso8601.format(nowMillis(), I18nConfiguration(TimeZone.Tokyo, Locale.Germany))).isEqualTo("2021-03-28T06:45:23.002+09:00[Asia/Tokyo]")
  }

  @Test
  fun testPlus(virtualNowProvider: VirtualNowProvider) {
    assertThat(virtualNowProvider.nowMillis()).isEqualTo(VirtualTime.defaultNow)

    virtualNowProvider.advanceBy(100.0)
    assertThat(virtualNowProvider.nowMillis()).isEqualTo(VirtualTime.defaultNow + 100.0)

    virtualNowProvider.advanceBy(0.0)
    assertThat(virtualNowProvider.nowMillis()).isEqualTo(VirtualTime.defaultNow + 100.0)
  }
}
