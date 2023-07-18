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
import com.meistercharts.geometry.Coordinates
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class CoordinatesCalculationsTest {
  @Test
  fun testIt() {
    assertThat(Coordinates.origin.distanceToLine(Coordinates.origin, Coordinates.origin)).isZero()
  }

  @Test
  fun `distanceToLine before`() {
    val lineStart = Coordinates(0.0, 1.0)
    val lineEnd = Coordinates(4.0, 1.0)

    assertThat(Coordinates(-2.0, 1.0).distanceToLine(lineStart, lineEnd)).isEqualTo(2.0) //left of line
    assertThat(Coordinates(5.0, 1.0).distanceToLine(lineStart, lineEnd)).isEqualTo(1.0) //right of line
    assertThat(Coordinates(6.0, 1.0).distanceToLine(lineStart, lineEnd)).isEqualTo(2.0) //right of line
    assertThat(Coordinates(0.0, 2.0).distanceToLine(lineStart, lineEnd)).isEqualTo(1.0) //above the line

    assertThat(Coordinates(-2.0, 3.0).distanceToLine(lineStart, lineEnd)).isEqualTo(sqrt(2.0 * 2.0 + 2.0 * 2.0)) //above, left
  }

  @Test
  fun `distanceToLine before2`() {
    val lineStart = Coordinates(1.0, 1.0)
    val lineEnd = Coordinates(4.0, 1.0)

    assertThat(Coordinates(-2.0, 1.0).distanceToLine(lineStart, lineEnd)).isEqualTo(3.0) //left of line
    assertThat(Coordinates(5.0, 1.0).distanceToLine(lineStart, lineEnd)).isEqualTo(1.0) //right of line
    assertThat(Coordinates(6.0, 1.0).distanceToLine(lineStart, lineEnd)).isEqualTo(2.0) //right of line
    assertThat(Coordinates(1.0, 2.0).distanceToLine(lineStart, lineEnd)).isEqualTo(1.0) //above the line

    assertThat(Coordinates(-1.0, 3.0).distanceToLine(lineStart, lineEnd)).isEqualTo(sqrt(2.0 * 2.0 + 2.0 * 2.0)) //above, left
  }

  @Test
  fun `distanceToLine should return 0 for point on line`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(4.0, 0.0)

    assertThat(Coordinates(2.0, 0.0).distanceToLine(lineStart, lineEnd)).isZero()
  }

  @Test
  fun `distanceToLine should return correct distance for point above line`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(4.0, 0.0)

    assertThat(Coordinates(2.0, 2.0).distanceToLine(lineStart, lineEnd)).isEqualTo(2.0)
  }

  @Test
  fun `distanceToLine should return correct distance for point below line`() {
    val lineStart = Coordinates(0.0, 4.0)
    val lineEnd = Coordinates(4.0, 4.0)

    assertThat(Coordinates(2.0, 2.0).distanceToLine(lineStart, lineEnd)).isEqualTo(2.0)
  }

  @Test
  fun `testDiagonal`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(4.0, 4.0)

    assertThat(Coordinates(2.0, 2.0).distanceToLine(lineStart, lineEnd)).isZero()
    assertThat(Coordinates(2.0, 1.0).distanceToLine(lineStart, lineEnd)).isCloseTo(sqrt(2.0) / 2.0, 0.00000001)
  }

  @Test
  fun `distanceToLine should return correct distance for point not aligned with line`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(4.0, 4.0)

    assertThat(lineStart.distanceTo(lineEnd)).isEqualTo(sqrt(32.0))

    val side = sqrt(32.0) / 2.0
    val hypo = 4.0
    val expectedResult = sqrt(hypo * hypo - side * side)

    assertThat(Coordinates(4.0, 0.0).distanceToLine(lineStart, lineEnd)).isCloseTo(expectedResult, 0.000000001)
  }

  @Test
  fun `distanceToLine should return correct distance for point aligned with line 2`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(8.0, 4.0)

    assertThat(Coordinates(4.0, 2.0).distanceToLine(lineStart, lineEnd)).isZero()
  }

  @Test
  fun `distanceToLine should return correct distance for point not aligned with line 2`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(8.0, 4.0)

    assertThat(Coordinates(3.0, 4.0).distanceToLine(lineStart, lineEnd)).isCloseTo(sqrt(5.0), 0.000000001)
  }

  @Test
  fun `distanceToLine should return correct distance for point not aligned with line 3`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(-8.0, -4.0)

    assertThat(Coordinates(-3.0, -4.0).distanceToLine(lineStart, lineEnd)).isCloseTo(sqrt(5.0), 0.000000001)
  }

  @Test
  fun `distanceToLine should return correct distance for point not aligned with line 4`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(-8.0, 4.0)

    assertThat(Coordinates(-4.0, -3.0).distanceToLine(lineStart, lineEnd)).isCloseTo(2.0 * sqrt(5.0), 0.000000001)
  }

  @Test
  fun `distanceToLine should return correct distance for point not aligned with line 5`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(8.0, -4.0)

    assertThat(Coordinates(-4.0, -3.0).distanceToLine(lineStart, lineEnd)).isCloseTo(5.0, 0.000000001)
  }
}



