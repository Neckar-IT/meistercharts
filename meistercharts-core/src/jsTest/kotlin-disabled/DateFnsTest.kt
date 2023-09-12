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
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.meistercharts.algorithms.time.timeZoneOffsetProvider
import it.neckar.datetime.minimal.TimeZone
import it.neckar.open.unit.si.ms
import kotlin.js.Date
import kotlin.test.Test

/**
 *
 */
class DateFnsTest {
  /**
   * From merge request: [https://github.com/date-fns/date-fns/pull/707/files]
   */
  @Test
  fun testsInspiredByForDateFns() {
    val now: Double = 1615761971002.0

    val date = Date(now)
    assertThat(date.toISOString()).isEqualTo("2021-03-14T22:46:11.002Z")

    assertThat(date.getTime()).isEqualTo(now)
    assertThat(date.atTimeZone(TimeZone.Berlin).toISOString()).isEqualTo("2021-03-14T21:46:11.002Z")

    assertThat(date.getUTCFullYear()).isEqualTo(2021)
    assertThat(date.getUTCHours()).isEqualTo(22)

    assertThat(1).isEqualTo(13)
  }

  /**
   * Returns a new date object that represents the date at local time!
   */
  fun Date.atTimeZone(timeZone: TimeZone): Date {
    if (timeZone == TimeZone.UTC) {
      return this
    }

    @ms val offset = timeZoneOffsetProvider.timeZoneOffset(getTime(), timeZone)
    return this.addMilliSeconds(offset)
  }

  /**
   * Returns a new date with additional milli seconds
   */
  fun Date.addMilliSeconds(additionalMillis: Double): Date {
    return Date(this.getTime() + additionalMillis)
  }

  fun Date.addMinutes(additionalMinutes: Double): Date {
    return addMilliSeconds(additionalMinutes * 60 * 1000.0)
  }
}
