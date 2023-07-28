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
package com.meistercharts.algorithms.layout

import assertk.*
import assertk.assertions.*
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.open.kotlin.lang.toRadians
import org.junit.jupiter.api.Test


/**
 */
class RectangleTest {
  @Test
  fun testWithin() {

    assertThat(Rectangle.isPointWithin(0.0, 0.0, 0.0, 0.0, 10.0, 10.0)).isTrue()
    assertThat(Rectangle.isPointWithin(1.0, 1.0, 0.0, 0.0, 10.0, 10.0)).isTrue()
    assertThat(Rectangle.isPointWithin(10.0, 10.0, 0.0, 0.0, 10.0, 10.0)).isTrue()

    assertThat(Rectangle.isPointWithin(0.0, -0.1, 0.0, 0.0, 10.0, 10.0)).isFalse()
    assertThat(Rectangle.isPointWithin(-0.1, 0.0, 0.0, 0.0, 10.0, 10.0)).isFalse()
    assertThat(Rectangle.isPointWithin(10.1, 10.0, 0.0, 0.0, 10.0, 10.0)).isFalse()
    assertThat(Rectangle.isPointWithin(10.0, 10.1, 0.0, 0.0, 10.0, 10.0)).isFalse()


    assertThat(Rectangle.isPointWithin(7.0, 7.0, 7.0, 7.0, 10.0, 10.0)).isTrue()
    assertThat(Rectangle.isPointWithin(7.1, 7.1, 7.0, 7.0, 10.0, 10.0)).isTrue()

    assertThat(Rectangle.isPointWithin(17.1, 7.0, 7.0, 7.0, 10.0, 10.0)).isFalse()
  }

  @Test
  fun testRectangleOverlap() {
    val other = Rectangle(100.0, 200.0, 50.0, 70.0)

    verifyOverlapping(Rectangle.zero, other, false)

    verifyOverlapping(Rectangle(100.0, 200.0, 0.0, 0.0), other, true)
    verifyOverlapping(Rectangle(99.0, 200.0, 0.0, 0.0), other, false)
    verifyOverlapping(Rectangle(101.0, 200.0, 0.0, 0.0), other, true)

    verifyOverlapping(Rectangle(100.0, 200.0, 0.0, 0.0), other, true)
    verifyOverlapping(Rectangle(100.0, 201.0, 0.0, 0.0), other, true)
    verifyOverlapping(Rectangle(99.0, 201.0, 0.0, 0.0), other, false)
    verifyOverlapping(Rectangle(100.0, 199.0, 0.0, 0.0), other, false)

    verifyOverlapping(Rectangle(150.0, 270.0, 0.0, 0.0), other, true)
    verifyOverlapping(Rectangle(151.0, 270.0, 0.0, 0.0), other, false)
    verifyOverlapping(Rectangle(151.0, 271.0, 0.0, 0.0), other, false)
    verifyOverlapping(Rectangle(149.0, 270.0, 0.0, 0.0), other, true)

    verifyOverlapping(Rectangle(150.0, 270.0, 0.0, 0.0), other, true)
    verifyOverlapping(Rectangle(150.0, 271.0, 0.0, 0.0), other, false)
    verifyOverlapping(Rectangle(150.0, 269.0, 0.0, 0.0), other, true)

    //within
    verifyOverlapping(Rectangle(125.0, 220.0, 0.0, 0.0), other, true)

    //span x
    verifyOverlapping(Rectangle(99.0, 220.0, 52.0, 0.0), other, true)
    //span y
    verifyOverlapping(Rectangle(125.0, 199.0, 0.0, 72.0), other, true)

    //overlap x
    verifyOverlapping(Rectangle(99.0, 220.0, 2.0, 0.0), other, true)
    verifyOverlapping(Rectangle(149.0, 220.0, 2.0, 0.0), other, true)

    //overlap y
    verifyOverlapping(Rectangle(125.0, 199.0, 0.0, 2.0), other, true)
    verifyOverlapping(Rectangle(125.0, 269.0, 0.0, 2.0), other, true)
  }

  private fun verifyOverlapping(one: Rectangle, other: Rectangle, overlappingExpected: Boolean) {
    //default order
    assertThat(one.overlaps(other)).isEqualTo(overlappingExpected)
    //reverse
    assertThat(other.overlaps(one)).isEqualTo(overlappingExpected)

    //doesNotOverlap

    //default order
    assertThat(one.doesNotOverlap(other)).isEqualTo(!overlappingExpected)
    //reverse
    assertThat(other.doesNotOverlap(one)).isEqualTo(!overlappingExpected)
  }

  @Test
  fun testExpand() {
    Rectangle.centered(Size(10.0, 5.0)).let {
      assertThat(it.left).isEqualTo(-5.0)
      assertThat(it.right).isEqualTo(5.0)
      assertThat(it.top).isEqualTo(-2.5)
      assertThat(it.bottom).isEqualTo(2.5)

      //Now expand
      it.expand(1.0, 2.0, 3.0, 4.0).let { expanded ->
        assertThat(expanded.left).isEqualTo(-5.0 - 1)
        assertThat(expanded.right).isEqualTo(5.0 + 3)
        assertThat(expanded.top).isEqualTo(-2.5 - 2)
        assertThat(expanded.bottom).isEqualTo(2.5 + 4)
      }
    }
  }

  @Test
  fun testCreation() {
    Rectangle.bottomRight(Size(10.0, 5.0)).let {
      assertThat(it.size).isEqualTo(Size(10.0, 5.0))

      assertThat(it.left).isEqualTo(-10.0)
      assertThat(it.top).isEqualTo(-5.0)
      assertThat(it.right).isEqualTo(0.0)
      assertThat(it.bottom).isEqualTo(0.0)
    }

    Rectangle.bottomLeft(Size(10.0, 5.0)).let {
      assertThat(it.size).isEqualTo(Size(10.0, 5.0))

      assertThat(it.left).isEqualTo(0.0)
      assertThat(it.top).isEqualTo(-5.0)
      assertThat(it.right).isEqualTo(10.0)
      assertThat(it.bottom).isEqualTo(0.0)
    }

    Rectangle.topRight(Size(10.0, 5.0)).let {
      assertThat(it.size).isEqualTo(Size(10.0, 5.0))

      assertThat(it.left).isEqualTo(-10.0)
      assertThat(it.top).isEqualTo(0.0)
      assertThat(it.right).isEqualTo(0.0)
      assertThat(it.bottom).isEqualTo(5.0)
    }
  }

  @Test
  internal fun testRelativePosition() {
    Rectangle(10.0, 20.0, 100.0, 150.0).also { boundingBox ->
      assertThat(boundingBox.findCoordinatesRelative(0.0, 0.0)).isEqualTo(Coordinates.of(10.0, 20.0))
      assertThat(boundingBox.findCoordinatesRelative(1.0, 1.0)).isEqualTo(Coordinates.of(10 + 100.0, 20 + 150.0))
      assertThat(boundingBox.findCoordinatesRelative(0.2, 0.8)).isEqualTo(Coordinates.of(10 + 100.0 * 0.2, 20 + 150.0 * 0.8))
    }
  }

  @Test
  internal fun testDirection() {
    Rectangle(0.0, 0.0, 100.0, 150.0).also { boundingBox ->
      assertThat(boundingBox.findCoordinates(Direction.TopLeft)).isEqualTo(Coordinates.of(0.0, 0.0))
      assertThat(boundingBox.findCoordinates(Direction.BottomRight)).isEqualTo(Coordinates.of(100.0, 150.0))
      assertThat(boundingBox.findCoordinates(Direction.Center)).isEqualTo(Coordinates.of(50.0, 75.0))
    }

    Rectangle(10.0, 20.0, 100.0, 150.0).also { boundingBox ->
      assertThat(boundingBox.findCoordinates(Direction.TopLeft)).isEqualTo(Coordinates.of(10.0, 20.0))
      assertThat(boundingBox.findCoordinates(Direction.BottomRight)).isEqualTo(Coordinates.of(10 + 100.0, 20 + 150.0))
      assertThat(boundingBox.findCoordinates(Direction.Center)).isEqualTo(Coordinates.of(10 + 50.0, 20 + 75.0))
    }
  }

  @Test
  internal fun testIt() {
    Rectangle.centered(100.0, 100.0).let {
      assertThat(it.xFromRadRelative((180.0 + 10.0).toRadians())).isCloseTo(-50.0, 0.01)
    }
  }

  @Test
  internal fun testFindBoundingBoxFromAngle() {
    Rectangle.centered(100.0, 100.0).let {

      //To the right
      assertThat(it.xFromRadRelative(0.0)).isEqualTo(50.0)

      //right right bottom
      assertThat(it.xFromRadRelative(30.0.toRadians())).isEqualTo(50.0)
      assertThat(it.xFromRadRelative(45.0.toRadians())).isEqualTo(50.0)
      assertThat(it.xFromRadRelative(60.0.toRadians())).isCloseTo(28.86, 0.01)

      //to the bottom
      assertThat(it.xFromRadRelative(90.0.toRadians())).isCloseTo(0.0, 0.01)

      //to left bottom
      assertThat(it.xFromRadRelative(100.0.toRadians())).isCloseTo(-8.81, 0.01)
      assertThat(it.xFromRadRelative((90.0 + 45.0).toRadians())).isCloseTo(-50.0, 0.01)
      assertThat(it.xFromRadRelative((90.0 + 45.0 + 10.0).toRadians())).isCloseTo(-50.0, 0.01)

      //to the left
      assertThat(it.xFromRadRelative(180.0.toRadians())).isCloseTo(-50.0, 0.01)

      //to the top left
      assertThat(it.xFromRadRelative((180.0 + 10.0).toRadians())).isCloseTo(-50.0, 0.01)
      assertThat(it.xFromRadRelative((180.0 + 45.0).toRadians())).isCloseTo(-50.0, 0.01)
      assertThat(it.xFromRadRelative((180.0 + 45.0 + 10.0).toRadians())).isCloseTo(-35.01, 0.01)

      //To the top
      assertThat(it.xFromRadRelative(270.0.toRadians())).isCloseTo(0.0, 0.01)

      //To the top right
      assertThat(it.xFromRadRelative((270.0 + 10.0).toRadians())).isCloseTo(8.82, 0.01)
      assertThat(it.xFromRadRelative((270.0 + 45.0).toRadians())).isCloseTo(50.0, 0.01)
      assertThat(it.xFromRadRelative((270.0 + 45.0 + 10.0).toRadians())).isCloseTo(50.0, 0.01)
    }
  }

  @Test
  internal fun testCenteredConstructor() {
    Rectangle.centered(100.0, 20.0).let {
      assertThat(it.left).isEqualTo(-50.0)
      assertThat(it.right).isEqualTo(50.0)
      assertThat(it.top).isEqualTo(-10.0)
      assertThat(it.bottom).isEqualTo(10.0)
    }
  }

  @Test
  internal fun testConstructors() {
    val rect = Rectangle(Coordinates(5.0, 5.0), Size(10.0, 10.0))

    assertThat(rect.left).isEqualTo(5.0)
    assertThat(rect.right).isEqualTo(15.0)
    assertThat(rect.top).isEqualTo(5.0)
    assertThat(rect.bottom).isEqualTo(15.0)
  }

  @Test
  internal fun testBOundingBoxStuff() {
    val rect = Rectangle(Coordinates(-5.0, -3.0), Size(12.0, 25.0))

    assertThat(rect.left).isEqualTo(-5.0)
    assertThat(rect.right).isEqualTo(7.0)
    assertThat(rect.top).isEqualTo(-3.0)
    assertThat(rect.bottom).isEqualTo(22.0)
  }

  @Test
  fun testGeometricDirectionTopDown() {
    val rectangle = Rectangle(10.0, 20.0, 200.0, 100.0)

    assertThat(rectangle.top).isEqualTo(20.0)
    assertThat(rectangle.bottom).isEqualTo(120.0)
  }

  @Test
  fun testGeometricPositiveStuff() {
    val rectangle = Rectangle(10.0, 20.0, 200.0, 100.0)

    assertThat(rectangle.left).isEqualTo(10.0)
    assertThat(rectangle.right).isEqualTo(210.0)
    assertThat(rectangle.top).isEqualTo(20.0)
    assertThat(rectangle.bottom).isEqualTo(120.0)

    assertThat(rectangle.widthAbs).isEqualTo(200.0)
    assertThat(rectangle.heightAbs).isEqualTo(100.0)
  }

  @Test
  fun testGeometricNegativeWidth() {
    val rectangle = Rectangle(10.0, 20.0, -200.0, 100.0)

    assertThat(rectangle.left).isEqualTo(10.0 - 200.0)
    assertThat(rectangle.right).isEqualTo(10.0)
    assertThat(rectangle.top).isEqualTo(20.0)
    assertThat(rectangle.bottom).isEqualTo(120.0)

    assertThat(rectangle.widthAbs).isEqualTo(200.0)
    assertThat(rectangle.heightAbs).isEqualTo(100.0)
  }

  @Test
  fun testGeometricNegativeHeight() {
    val rectangle = Rectangle(10.0, 20.0, 200.0, -100.0)

    assertThat(rectangle.left).isEqualTo(10.0)
    assertThat(rectangle.right).isEqualTo(210.0)
    assertThat(rectangle.top).isEqualTo(20.0 - 100)
    assertThat(rectangle.bottom).isEqualTo(20.0)

    assertThat(rectangle.widthAbs).isEqualTo(200.0)
    assertThat(rectangle.heightAbs).isEqualTo(100.0)
  }

  @Test
  fun testBasics() {
    val rectangle = Rectangle(10.0, 20.0, 100.0, 100.0)
    assertThat(rectangle.getX()).isEqualTo(10.0)
    assertThat(rectangle.getY()).isEqualTo(20.0)
  }

  @Test
  fun testEquals() {
    val rectangle = Rectangle(10.0, 20.0, 100.0, 100.0)
    val ltrb = Rectangle.withLTRB(10.0, 20.0, 110.0, 120.0)
    assertThat(rectangle).isEqualTo(ltrb)
  }

  @Test
  fun testContains() {
    val rectangle = Rectangle(10.0, 20.0, 100.0, 100.0)
    assertThat(rectangle.contains(5.0, 20.0)).isFalse()
    assertThat(rectangle.contains(10.0, 10.0)).isFalse()
    assertThat(rectangle.contains(10.0, 20.0)).isTrue()
    assertThat(rectangle.contains(110.0, 120.0)).isTrue()
    assertThat(rectangle.contains(55.0, 60.0)).isTrue()
  }

  @Test
  fun testContainsWithNegativeSize() {
    val rectangle = Rectangle(10.0, 20.0, -100.0, -100.0)

    assertThat(rectangle.contains(-100.0, -90.0)).isFalse() // top left
    assertThat(rectangle.contains(-50.0, -90.0)).isFalse() // top
    assertThat(rectangle.contains(20.0, -90.0)).isFalse() // top right
    assertThat(rectangle.contains(-100.0, -60.0)).isFalse() // left
    assertThat(rectangle.contains(20.0, -60.0)).isFalse() // right
    assertThat(rectangle.contains(-100.0, 40.0)).isFalse() // left bottom
    assertThat(rectangle.contains(-60.0, 40.0)).isFalse() // bottom
    assertThat(rectangle.contains(20.0, 40.0)).isFalse() // bottom right
    assertThat(rectangle.contains(-60.0, -60.0)).isTrue() // center
  }
}
