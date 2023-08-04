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
package com.meistercharts.model

import assertk.*
import assertk.assertions.*
import it.neckar.geometry.PolarCoordinates
import it.neckar.open.kotlin.lang.toRadians
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.sqrt

internal class PolarCoordinatesTest {

  @Test
  internal fun testDirectionTopBottom() {
    assertThat(PolarCoordinates.isToTheTop(-0.1.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheTop(0.0.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheTop(0.1.toRadians())).isFalse()

    assertThat(PolarCoordinates.isToTheTop(90.0.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheTop(179.9.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheTop(180.0.toRadians())).isFalse()

    assertThat(180.1.toRadians()).isGreaterThan(PI)
    assertThat(PolarCoordinates.isToTheTop(180.1.toRadians())).isTrue()

    assertThat(PolarCoordinates.isToTheTop(269.9.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheTop(270.0.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheTop(270.1.toRadians())).isTrue()

    assertThat(PolarCoordinates.isToTheTop(359.9.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheTop(360.0.toRadians())).isFalse()

    assertThat(PolarCoordinates.isToTheTop(360.1.toRadians())).isFalse()

    assertThat(PolarCoordinates.isToTheTop(719.9.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheTop(720.0.toRadians())).isFalse()
  }

  @Test
  internal fun testDirectionTopBottomNegative() {
    assertThat(PolarCoordinates.isToTheTop(0.1.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheTop(0.0.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheTop(-0.1.toRadians())).isTrue()


    assertThat(PolarCoordinates.isToTheTop(-90.0.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheTop(-179.9.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheTop(-180.0.toRadians())).isFalse()

    assertThat(-180.1.toRadians()).isLessThan(-PI)
    assertThat(PolarCoordinates.isToTheTop(-180.1.toRadians())).isFalse()

    assertThat(PolarCoordinates.isToTheTop(-269.9.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheTop(-270.0.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheTop(-270.1.toRadians())).isFalse()

    assertThat(PolarCoordinates.isToTheTop(-359.9.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheTop(-360.0.toRadians())).isFalse()

    assertThat(PolarCoordinates.isToTheTop(-360.1.toRadians())).isTrue()
  }

  @Test
  internal fun testDirectionLeftRight() {
    assertThat(PolarCoordinates.isToTheRight(-0.1.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheRight(0.0.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheRight(0.1.toRadians())).isTrue()

    assertThat(PolarCoordinates.isToTheRight(90.0.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheRight(179.9.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheRight(180.0.toRadians())).isFalse()

    assertThat(PolarCoordinates.isToTheRight(180.1.toRadians())).isFalse()

    assertThat(PolarCoordinates.isToTheRight(269.9.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheRight(270.0.toRadians())).isFalse()
    assertThat(270.1.toRadians()).isGreaterThan(PI / 4.0 * 6.0)
    assertThat(PolarCoordinates.isToTheRight(270.1.toRadians())).isTrue()

    assertThat(PolarCoordinates.isToTheRight(359.9.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheRight(360.0.toRadians())).isTrue()

    assertThat(PolarCoordinates.isToTheRight(360.1.toRadians())).isTrue()

    assertThat(PolarCoordinates.isToTheRight(719.9.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheRight(720.0.toRadians())).isTrue()
  }

  @Test
  internal fun testDirectionLeftRightNegative() {
    assertThat(PolarCoordinates.isToTheRight(0.1.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheRight(0.0.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheRight(-0.1.toRadians())).isTrue()

    assertThat(PolarCoordinates.isToTheRight(-90.0.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheRight(-179.9.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheRight(-180.0.toRadians())).isFalse()

    assertThat(PolarCoordinates.isToTheRight(-180.1.toRadians())).isFalse()

    assertThat(PolarCoordinates.isToTheRight(-269.9.toRadians())).isFalse()
    assertThat(PolarCoordinates.isToTheRight(-270.0.toRadians())).isFalse()
    assertThat(-270.0.toRadians()).isEqualTo(-PI * 1.5)
    assertThat(-270.1.toRadians()).isLessThan(-PI * 1.5)
    assertThat(PolarCoordinates.isToTheRight(-270.1.toRadians())).isTrue()

    assertThat(PolarCoordinates.isToTheRight(-359.9.toRadians())).isTrue()
    assertThat(PolarCoordinates.isToTheRight(-360.0.toRadians())).isTrue()

    assertThat(PolarCoordinates.isToTheRight(-360.1.toRadians())).isTrue()
  }

  @Test
  fun toCartesian() {
    assertThat(PolarCoordinates(0.0, 0.0).toCartesian().x).isCloseTo(0.0, 0.00001)
    assertThat(PolarCoordinates(0.0, 0.0).toCartesian().y).isCloseTo(0.0, 0.00001)

    assertThat(PolarCoordinates(sqrt(2.0), PI / 4.0).toCartesian().x).isCloseTo(1.0, 0.00001)
    assertThat(PolarCoordinates(sqrt(2.0), PI / 4.0).toCartesian().y).isCloseTo(1.0, 0.00001)

    assertThat(PolarCoordinates(sqrt(2.0), 3.0 * PI / 4.0).toCartesian().x).isCloseTo(-1.0, 0.00001)
    assertThat(PolarCoordinates(sqrt(2.0), 3.0 * PI / 4.0).toCartesian().y).isCloseTo(1.0, 0.00001)

    assertThat(PolarCoordinates(sqrt(2.0), 5.0 * PI / 4.0).toCartesian().x).isCloseTo(-1.0, 0.00001)
    assertThat(PolarCoordinates(sqrt(2.0), 5.0 * PI / 4.0).toCartesian().y).isCloseTo(-1.0, 0.00001)

    assertThat(PolarCoordinates(sqrt(2.0), 7.0 * PI / 4.0).toCartesian().x).isCloseTo(1.0, 0.00001)
    assertThat(PolarCoordinates(sqrt(2.0), 7.0 * PI / 4.0).toCartesian().y).isCloseTo(-1.0, 0.00001)

    assertThat(PolarCoordinates(1.0, PI).toCartesian().x).isCloseTo(-1.0, 0.00001)
    assertThat(PolarCoordinates(1.0, PI).toCartesian().y).isCloseTo(0.0, 0.00001)
  }

  @Test
  fun roundTrip() {
    val source = PolarCoordinates(1.0, PI)
    val cartesian = source.toCartesian()
    val roundTrip = cartesian.toPolar()

    assertEquals(source.r, roundTrip.r, 0.00001)
    assertEquals(source.theta, roundTrip.theta, 0.00001)
  }
}
