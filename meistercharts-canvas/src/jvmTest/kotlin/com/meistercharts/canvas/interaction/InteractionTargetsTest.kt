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
package com.meistercharts.canvas.interaction

import assertk.*
import assertk.assertions.*
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Rectangle
import it.neckar.geometry.RightTriangleType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InteractionTargetsTest {

  private lateinit var interactionTargets: InteractionTargets<TestElement>

  private val firstElement: TestElement = TestElement("first")
  private val secondElement: TestElement = TestElement("second")

  @BeforeEach
  fun setUp() {
    interactionTargets = InteractionTargets()
  }

  @Test
  fun `returns null when nothing is registered`() {
    assertThat(interactionTargets.findTargetAt(Coordinates(10.0, 10.0))).isNull()
  }

  @Test
  fun `returns null outside of every registered region`() {
    interactionTargets.register(firstElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0))

    assertThat(interactionTargets.findTargetAt(Coordinates(200.0, 200.0))).isNull()
  }

  @Test
  fun `finds the element whose body contains the location`() {
    interactionTargets.register(firstElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0))
    interactionTargets.register(secondElement, HandleRole.Body, Rectangle(200.0, 0.0, 100.0, 100.0))

    assertThat(interactionTargets.findTargetAt(Coordinates(250.0, 50.0))?.element).isEqualTo(secondElement)
  }

  @Test
  fun `a button wins over a body that contains it`() {
    interactionTargets.register(firstElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0))
    interactionTargets.register(firstElement, HandleRole.DeleteButton, Rectangle(40.0, 40.0, 20.0, 20.0))

    val target = interactionTargets.findTargetAt(Coordinates(50.0, 50.0))

    assertThat(target?.role).isEqualTo(HandleRole.DeleteButton)
  }

  @Test
  fun `a button wins over a body registered after it`() {
    interactionTargets.register(firstElement, HandleRole.DeleteButton, Rectangle(40.0, 40.0, 20.0, 20.0))
    interactionTargets.register(secondElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0))

    val target = interactionTargets.findTargetAt(Coordinates(50.0, 50.0))

    assertThat(target?.role).isEqualTo(HandleRole.DeleteButton)
    assertThat(target?.element).isEqualTo(firstElement)
  }

  @Test
  fun `the region registered last wins between overlapping bodies`() {
    interactionTargets.register(firstElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0))
    interactionTargets.register(secondElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0))

    assertThat(interactionTargets.findTargetAt(Coordinates(50.0, 50.0))?.element).isEqualTo(secondElement)
  }

  @Test
  fun `the region registered last wins between overlapping buttons of the same priority`() {
    interactionTargets.register(firstElement, HandleRole.RotateButton, Rectangle(40.0, 40.0, 20.0, 20.0))
    interactionTargets.register(firstElement, HandleRole.DeleteButton, Rectangle(40.0, 40.0, 20.0, 20.0))

    assertThat(interactionTargets.findTargetAt(Coordinates(50.0, 50.0))?.role).isEqualTo(HandleRole.DeleteButton)
  }

  /**
   * The defect this class replaces: hover walked the button roles in a different order than press,
   * so an overlapping rotate and delete button highlighted one action and executed the other.
   */
  @Test
  fun `repeated lookups of overlapping rotate and delete buttons return the same target`() {
    interactionTargets.register(firstElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0))
    interactionTargets.register(firstElement, HandleRole.RotateButton, Rectangle(40.0, 40.0, 20.0, 20.0))
    interactionTargets.register(firstElement, HandleRole.DeleteButton, Rectangle(45.0, 45.0, 20.0, 20.0))

    val hoverTarget = interactionTargets.findTargetAt(Coordinates(50.0, 50.0))
    val pressTarget = interactionTargets.findTargetAt(Coordinates(50.0, 50.0))

    assertThat(hoverTarget).isEqualTo(pressTarget)
    assertThat(hoverTarget?.role).isEqualTo(HandleRole.DeleteButton)
  }

  @Test
  fun `beginLayout drops the regions of the previous pass`() {
    interactionTargets.register(firstElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0))
    interactionTargets.beginLayout()
    interactionTargets.register(secondElement, HandleRole.Body, Rectangle(200.0, 0.0, 100.0, 100.0))

    assertThat(interactionTargets.size).isEqualTo(1)
    assertThat(interactionTargets.findTargetAt(Coordinates(50.0, 50.0))).isNull()
    assertThat(interactionTargets.findTargetAt(Coordinates(250.0, 50.0))?.element).isEqualTo(secondElement)
  }

  @Test
  fun `findElementAt only considers the requested role`() {
    interactionTargets.register(firstElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0))
    interactionTargets.register(firstElement, HandleRole.DeleteButton, Rectangle(40.0, 40.0, 20.0, 20.0))

    assertThat(interactionTargets.findElementAt(Coordinates(50.0, 50.0), HandleRole.Body)).isEqualTo(firstElement)
    assertThat(interactionTargets.findElementAt(Coordinates(10.0, 10.0), HandleRole.DeleteButton)).isNull()
  }

  @Test
  fun `a triangular region only covers the filled side of its hypotenuse`() {
    interactionTargets.register(firstElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0), RightTriangleType.MissingCornerInFirstQuadrant)

    //The hypotenuse of this triangle runs from (100, 0) to (0, 100); the corner at (0, 0) is the missing one
    assertThat(interactionTargets.findTargetAt(Coordinates(90.0, 90.0))?.element).isEqualTo(firstElement)
    assertThat(interactionTargets.findTargetAt(Coordinates(10.0, 10.0))).isNull()
  }

  @Test
  fun `findElementAt honours the triangular region`() {
    interactionTargets.register(firstElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0), RightTriangleType.MissingCornerInFirstQuadrant)

    assertThat(interactionTargets.findElementAt(Coordinates(90.0, 90.0), HandleRole.Body)).isEqualTo(firstElement)
    assertThat(interactionTargets.findElementAt(Coordinates(10.0, 10.0), HandleRole.Body)).isNull()
  }

  @Test
  fun `a rectangular region registered on top of a triangular one is found in the missing corner`() {
    interactionTargets.register(firstElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0), RightTriangleType.MissingCornerInFirstQuadrant)
    interactionTargets.register(secondElement, HandleRole.Body, Rectangle(0.0, 0.0, 100.0, 100.0))

    assertThat(interactionTargets.findTargetAt(Coordinates(10.0, 10.0))?.element).isEqualTo(secondElement)
  }

  @Test
  fun `negative width and height describe the same region as positive ones`() {
    interactionTargets.register(firstElement, HandleRole.Body, Rectangle(100.0, 100.0, -100.0, -100.0))

    assertThat(interactionTargets.findTargetAt(Coordinates(50.0, 50.0))?.element).isEqualTo(firstElement)
  }

  private data class TestElement(val name: String)
}
