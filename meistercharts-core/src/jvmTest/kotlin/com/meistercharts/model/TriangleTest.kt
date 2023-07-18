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
import com.meistercharts.geometry.Rectangle
import com.meistercharts.geometry.RightTriangleType.MissingCornerInFirstQuadrant
import com.meistercharts.geometry.RightTriangleType.MissingCornerInFourthQuadrant
import com.meistercharts.geometry.RightTriangleType.MissingCornerInSecondQuadrant
import com.meistercharts.geometry.RightTriangleType.MissingCornerInThirdQuadrant
import com.meistercharts.geometry.Triangle
import org.junit.jupiter.api.Test

class TriangleTest {
  @Test
  fun testSimpleTriangleCollision() {
    val testTriangle1 = Triangle(100.0, 200.0, 100.0, 200.0, MissingCornerInFirstQuadrant)
    val testTriangle2 = Triangle(1000.0, 200.0, 1000.0, 2000.0, MissingCornerInSecondQuadrant)
    val testTriangle3 = Triangle(-100.0, -200.0, -600.0, -200.0, MissingCornerInThirdQuadrant)
    val testTriangle4 = Triangle(-3000.0, -5000.0, 4000.0, 10000.0, MissingCornerInFourthQuadrant)

    val testRectangle1 = Rectangle(100.0, 200.0, 100.0, 200.0)
    val testRectangle2 = Rectangle(-100.0, -200.0, -100.0, -200.0)
    val testRectangle3 = Rectangle(-1000.0, -2000.0, 5000.0, 3000.0)

    assertThat(testTriangle1.overlaps(testRectangle1)).isEqualTo(true)
    assertThat(testTriangle1.overlaps(testRectangle2)).isEqualTo(false)
    assertThat(testTriangle1.overlaps(testRectangle3)).isEqualTo(true)

    assertThat(testTriangle2.overlaps(testRectangle1)).isEqualTo(false)
    assertThat(testTriangle2.overlaps(testRectangle2)).isEqualTo(false)
    assertThat(testTriangle2.overlaps(testRectangle3)).isEqualTo(true)

    assertThat(testTriangle3.overlaps(testRectangle1)).isEqualTo(false)
    assertThat(testTriangle3.overlaps(testRectangle2)).isEqualTo(true)
    assertThat(testTriangle3.overlaps(testRectangle3)).isEqualTo(true)

    assertThat(testTriangle4.overlaps(testRectangle1)).isEqualTo(true)
    assertThat(testTriangle4.overlaps(testRectangle2)).isEqualTo(true)
    assertThat(testTriangle4.overlaps(testRectangle3)).isEqualTo(true)
  }
}
