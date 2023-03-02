package com.meistercharts.algorithms.bezier

import assertk.*
import assertk.assertions.*
import com.meistercharts.model.Coordinates
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

