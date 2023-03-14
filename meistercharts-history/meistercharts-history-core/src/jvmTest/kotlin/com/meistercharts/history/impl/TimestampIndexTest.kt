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
package com.meistercharts.history.impl

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.TimestampIndex
import org.junit.jupiter.api.Test

class TimestampIndexTest {
  @Test
  fun testCompare() {
    assertThat(TimestampIndex(7)).isGreaterThan(TimestampIndex(6))
    assertThat(TimestampIndex(7)).isLessThan(TimestampIndex(8))
    assertThat(TimestampIndex(7)).isLessThanOrEqualTo(TimestampIndex(7))
    assertThat(TimestampIndex(7)).isGreaterThanOrEqualTo(TimestampIndex(7))
  }
}
