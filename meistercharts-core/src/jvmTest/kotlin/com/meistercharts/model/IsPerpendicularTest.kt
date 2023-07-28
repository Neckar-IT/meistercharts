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
import it.neckar.geometry.Coordinates
import it.neckar.geometry.isPerpendicularToLineSegment
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class IsPerpendicularTest {
  @Test
  fun testIt() {
    assertThat(Coordinates.origin.isPerpendicularToLineSegment(Coordinates.origin, Coordinates.origin)).isTrue()
  }

  @Test
  fun `isPerpendicularToLineSegment before`() {
    val lineStart = Coordinates(0.0, 1.0)
    val lineEnd = Coordinates(4.0, 1.0)

    assertThat(Coordinates(-2.0, 1.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isFalse() //left of line
    assertThat(Coordinates(5.0, 1.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isFalse() //right of line
    assertThat(Coordinates(6.0, 1.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isFalse() //right of line
    assertThat(Coordinates(0.0, 2.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue() //above the line

    assertThat(Coordinates(-2.0, 3.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isFalse() //above, left
  }

  @Test
  fun `isPerpendicularToLineSegment before2`() {
    val lineStart = Coordinates(1.0, 1.0)
    val lineEnd = Coordinates(4.0, 1.0)

    assertThat(Coordinates(-2.0, 1.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isFalse() //left of line
    assertThat(Coordinates(5.0, 1.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isFalse() //right of line
    assertThat(Coordinates(6.0, 1.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isFalse() //right of line
    assertThat(Coordinates(1.0, 2.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue() //above the line

    assertThat(Coordinates(-1.0, 3.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isFalse() //above, left
  }

  @Test
  fun `isPerpendicularToLineSegment should return true for point on line`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(4.0, 0.0)

    assertThat(Coordinates(2.0, 0.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue()
  }

  @Test
  fun `isPerpendicularToLineSegment should return true for point above line`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(4.0, 0.0)

    assertThat(Coordinates(2.0, 2.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue()
  }

  @Test
  fun `isPerpendicularToLineSegment should return true for point below line`() {
    val lineStart = Coordinates(0.0, 4.0)
    val lineEnd = Coordinates(4.0, 4.0)

    assertThat(Coordinates(2.0, 2.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue()
  }

  @Test
  fun `testDiagonal`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(4.0, 4.0)

    assertThat(Coordinates(2.0, 2.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue()
    assertThat(Coordinates(2.0, 1.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue()
  }

  @Test
  fun `isPerpendicularToLineSegment should return true for point not aligned with line`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(4.0, 4.0)

    assertThat(lineStart.distanceTo(lineEnd)).isEqualTo(sqrt(32.0))
    assertThat(Coordinates(4.0, 0.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue()
  }

  @Test
  fun `isPerpendicularToLineSegment should return true for point aligned with line 2`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(8.0, 4.0)

    assertThat(Coordinates(4.0, 2.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue()
  }

  @Test
  fun `isPerpendicularToLineSegment should return true for point not aligned with line 2`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(8.0, 4.0)

    assertThat(Coordinates(3.0, 4.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue()
  }

  @Test
  fun `isPerpendicularToLineSegment should return true for point not aligned with line 3`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(-8.0, -4.0)

    assertThat(Coordinates(-3.0, -4.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue()
  }

  @Test
  fun `isPerpendicularToLineSegment should return true for point not aligned with line 4`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(-8.0, 4.0)

    assertThat(Coordinates(-4.0, -3.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isTrue()
  }

  @Test
  fun `isPerpendicularToLineSegment should return true for point not aligned with line 5`() {
    val lineStart = Coordinates(0.0, 0.0)
    val lineEnd = Coordinates(8.0, -4.0)

    assertThat(Coordinates(-4.0, -3.0).isPerpendicularToLineSegment(lineStart, lineEnd)).isFalse()
  }
}



