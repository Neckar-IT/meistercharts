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
package com.meistercharts.charts

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 *
 */
class LinearPixel2domainRelativeCalculatorTest {
  @Test
  fun testC1() {
    val calculator = LinearPixel2domainRelativeCalculator(10)

    assertThat(calculator.pixel2domainRelative(0.0)).isEqualTo(0.0)
    assertThat(calculator.pixel2domainRelative(9.0)).isEqualTo(1.0)
  }

  @Test
  fun testC3() {
    val calculator = LinearPixel2domainRelativeCalculator(256)

    assertThat(calculator.pixel2domainRelative(0.0)).isEqualTo(0.0)
    assertThat(calculator.pixel2domainRelative(10.0)).isCloseTo(0.039, 0.001)
    assertThat(calculator.pixel2domainRelative(25.6)).isCloseTo(0.1, 0.01)
    assertThat(calculator.pixel2domainRelative(255.0)).isEqualTo(1.0)
  }

  @Test
  fun testC2() {
    val calculator = LinearPixel2domainRelativeCalculator(2)

    assertThat(calculator.pixel2domainRelative(0.0)).isEqualTo(0.0)
    assertThat(calculator.pixel2domainRelative(1.0)).isEqualTo(1.0)
    assertThat(calculator.pixel2domainRelative(0.5)).isEqualTo(0.5)
  }
}
