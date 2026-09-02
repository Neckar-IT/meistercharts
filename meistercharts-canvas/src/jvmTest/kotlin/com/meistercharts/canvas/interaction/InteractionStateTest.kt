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
import org.junit.jupiter.api.Test

class InteractionStateTest {

  private val element: TestElement = TestElement("first")

  @Test
  fun `an empty state has no gesture in progress`() {
    val state = InteractionState.empty<TestElement>()

    assertThat(state.drag).isNull()
    assertThat(state.draggedElement).isNull()
    assertThat(state.changingGeometry).isFalse()
  }

  @Test
  fun `a drag carries the element and the location it started at`() {
    val state = InteractionState(drag = InteractionState.Drag(element, Coordinates(30.0, 40.0)))

    assertThat(state.draggedElement).isEqualTo(element)
    assertThat(state.drag?.from).isEqualTo(Coordinates(30.0, 40.0))
    assertThat(state.changingGeometry).isTrue()
  }

  @Test
  fun `a resize carries the element and the geometry it started at`() {
    val state = InteractionState(resized = InteractionState.Resize(element, Rectangle(30.0, 40.0, 100.0, 200.0)))

    assertThat(state.resizedElement).isEqualTo(element)
    assertThat(state.resized?.from).isEqualTo(Rectangle(30.0, 40.0, 100.0, 200.0))
    assertThat(state.draggedElement).isNull()
    assertThat(state.changingGeometry).isTrue()
  }

  @Test
  fun `the short form names the dragged element and its anchor`() {
    val state = InteractionState(drag = InteractionState.Drag(element, Coordinates(30.0, 40.0)))

    assertThat(state.toString()).contains("drag=TestElement from 30.0/40.0")
  }

  private data class TestElement(val name: String)
}
