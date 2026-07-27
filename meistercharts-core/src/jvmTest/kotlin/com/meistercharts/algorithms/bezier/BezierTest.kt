/*
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
package com.meistercharts.algorithms.bezier

import assertk.*
import assertk.assertions.*
import com.meistercharts.geometry.Bezier
import com.meistercharts.geometry.length
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Rectangle
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

/**
 *
 */
class BezierTest {
  @Test
  fun testLengthStraight() {
    val start = Coordinates(0, 0)

    val control1 = Coordinates(50, 50)
    val control2 = Coordinates(50, 50)

    val end = Coordinates(100, 100)

    Bezier(start, control1, control2, end).let { bezier ->
      assertThat(bezier.length(steps = 100)).isCloseTo(100.0 * sqrt(2.0), 0.00001)
    }
  }

  @Test
  fun testLength() {
    val start = Coordinates(0, 0)

    val control1 = Coordinates(50, 0)
    val control2 = Coordinates(50, 100)

    val end = Coordinates(100, 100)

    Bezier(start, control1, control2, end).let { bezier ->
      assertThat(bezier.length(steps = 100)).isCloseTo(149.5, 0.1)

      assertThat(bezier.p0).isEqualTo(Coordinates.origin)
      assertThat(bezier.p1).isEqualTo(control1)
      assertThat(bezier.p2).isEqualTo(control2)
      assertThat(bezier.p3).isEqualTo(end)
    }
  }

  @Test
  fun `cubic bounds are x,y,width,height (not left,top,right,bottom)`() {
    //All control points lie inside the endpoint box, so the bounds are exactly that box.
    val bezier = Bezier(
      Coordinates(10, 20),
      Coordinates(20, 30),
      Coordinates(30, 50),
      Coordinates(40, 60),
    )

    val bounds = bezier.getBounds(Rectangle(0.0, 0.0, 0.0, 0.0))

    //x=10, y=20, width=40-10=30, height=60-20=40. The old code produced x=0/width=40.
    assertThat(bounds.getX()).isCloseTo(10.0, 0.0001)
    assertThat(bounds.getY()).isCloseTo(20.0, 0.0001)
    assertThat(bounds.getWidth()).isCloseTo(30.0, 0.0001)
    assertThat(bounds.getHeight()).isCloseTo(40.0, 0.0001)
  }

  @Test
  fun `quad bounds keep the curvature (no integer-division collapse)`() {
    //A symmetric arc peaking above the endpoints. If quadToCubic collapses (2/3 == 0),
    //the control points fall onto the endpoints and the bounds height shrinks to 0.
    val bezier = Bezier(
      Coordinates(0, 0),
      Coordinates(30, 60),
      Coordinates(60, 0),
    )

    val bounds = bezier.getBounds(Rectangle(0.0, 0.0, 0.0, 0.0))

    //The arc rises above y=0, so the box must have a positive height (was 0 with the 2/3 bug).
    assertThat(bounds.getHeight()).isGreaterThan(0.0)
    assertThat(bounds.getWidth()).isCloseTo(60.0, 0.0001)
  }
}

