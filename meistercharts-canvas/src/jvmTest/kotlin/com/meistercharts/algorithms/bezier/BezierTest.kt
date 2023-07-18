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
package com.meistercharts.algorithms.bezier

import assertk.*
import assertk.assertions.*
import com.meistercharts.canvas.geometry.Bezier
import com.meistercharts.canvas.geometry.length
import com.meistercharts.geometry.Coordinates
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
}

