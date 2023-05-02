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

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

/**
 *
 */
class LinearValueRangeTest {
  @Test
  internal fun testCenter() {
    assertThat(LinearValueRange(10.0, 20.0).center()).isEqualTo(15.0)
    assertThat(LinearValueRange(19.0, 20.0).center()).isEqualTo(19.5)
    assertThat(LinearValueRange(-10.0, 10.0).center()).isEqualTo(0.0)
  }

  @Test
  fun testDelta() {

    val valueRange = LinearValueRange(20.0, 30.0)

    assertThat(valueRange.deltaToDomainRelative(5.0)).isEqualTo(0.5)
    assertThat(valueRange.deltaToDomainRelative(0.0)).isEqualTo(0.0)
    assertThat(valueRange.deltaToDomainRelative(10.0)).isEqualTo(1.0)

    assertThat(valueRange.deltaToDomain(0.5)).isEqualTo(5.0)
    assertThat(valueRange.deltaToDomain(0.0)).isEqualTo(0.0)
    assertThat(valueRange.deltaToDomain(1.0)).isEqualTo(10.0)
  }
}
