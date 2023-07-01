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
package com.meistercharts.algorithms

import assertk.*
import assertk.assertions.*
import com.meistercharts.range.BinaryValueRange
import org.junit.jupiter.api.Test

/**
 */
internal class BinaryValueRangeTest {
  @Test
  fun testIt() {
    val range = BinaryValueRange

    assertThat(range.start).isEqualTo(0.0)
    assertThat(range.end).isEqualTo(1.0)
    assertThat(range.toDomainRelative(true)).isEqualTo(1.0)
    assertThat(range.toDomainRelative(false)).isEqualTo(0.0)

    assertThat(range.toDomain(1.0)).isEqualTo(1.0)
    assertThat(range.toDomain(0.0)).isEqualTo(0.0)
    assertThat(range.toDomainBoolean(1.0)).isEqualTo(true)
    assertThat(range.toDomainBoolean(0.0)).isEqualTo(false)
  }
}
