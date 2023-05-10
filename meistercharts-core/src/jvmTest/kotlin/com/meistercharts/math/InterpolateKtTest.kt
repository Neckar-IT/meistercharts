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
package com.cedarsoft.math

import assertk.*
import assertk.assertions.*
import it.neckar.open.kotlin.lang.interpolate
import it.neckar.open.kotlin.lang.relativeDistanceBetween
import org.junit.jupiter.api.Test

class InterpolateKtTest {
  @Test
  fun testInterpolate() {
    assertThat(0.5.interpolate(20.0, 60.0)).isEqualTo(40.0)
  }

  @Test
  fun testRelativeDistance() {
    assertThat(40.0.relativeDistanceBetween(20.0, 60.0)).isEqualTo(0.5)
    assertThat(40.0.relativeDistanceBetween(20.0, 40.0)).isEqualTo(1.0)

    assertThat(10.0.relativeDistanceBetween(10.0, 20.0)).isEqualTo(0.0)
    assertThat(11.0.relativeDistanceBetween(10.0, 20.0)).isEqualTo(0.1)
    assertThat(19.0.relativeDistanceBetween(10.0, 20.0)).isEqualTo(0.9)
    assertThat(20.0.relativeDistanceBetween(10.0, 20.0)).isEqualTo(1.0)
    assertThat(21.0.relativeDistanceBetween(10.0, 20.0)).isEqualTo(1.1)


    assertThat(10.0.relativeDistanceBetween(80.0, 100.0)).isEqualTo(-3.5)


    assertThat {
      40.0.relativeDistanceBetween(40.0, 40.0)
    }.isFailure()
  }

  @Test
  fun testInterpolateRoundTrip() {
    checkInterpolateRelativeDistance(50.0, 40.0, 60.0, 0.5)
    checkInterpolateRelativeDistance(30.0, 40.0, 60.0, -0.5)

    checkInterpolateRelativeDistance(50.0, 60.0, 40.0, 0.5)
    checkInterpolateRelativeDistance(70.0, 60.0, 40.0, -0.5)
  }

  private fun checkInterpolateRelativeDistance(value: Double, lowerBound: Double, upperBound: Double, expectedRelativeDistance: Double) {
    val relativeDistance = value.relativeDistanceBetween(lowerBound, upperBound)
    assertThat(relativeDistance).isEqualTo(expectedRelativeDistance)

    val valueReversed = relativeDistance.interpolate(lowerBound, upperBound)
    assertThat(valueReversed).isEqualTo(value)
  }
}
