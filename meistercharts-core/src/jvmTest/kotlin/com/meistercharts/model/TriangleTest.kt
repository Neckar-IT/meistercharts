package com.meistercharts.model

import assertk.*
import assertk.assertions.*
import com.meistercharts.model.RightTriangleType.MissingCornerInFirstQuadrant
import com.meistercharts.model.RightTriangleType.MissingCornerInFourthQuadrant
import com.meistercharts.model.RightTriangleType.MissingCornerInSecondQuadrant
import com.meistercharts.model.RightTriangleType.MissingCornerInThirdQuadrant
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
