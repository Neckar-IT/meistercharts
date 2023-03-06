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
package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.toDoubleMillis
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *
 */
class HistoryRangeTest {
  @Test
  fun testRanges() {
    val millis = ZonedDateTime.of(2020, 7, 30, 7, 12, 24, 0, ZoneId.of("Europe/Berlin")).toDoubleMillis()

    val descriptor = HistoryBucketDescriptor.forTimestamp(millis, HistoryBucketRange.NinetyYears)

    assertThat(descriptor.start.formatUtc()).isEqualTo("1970-01-01T00:00:00.000")
    assertThat(descriptor.end.formatUtc()).isEqualTo("2058-09-16T00:00:00.000")
  }
}
