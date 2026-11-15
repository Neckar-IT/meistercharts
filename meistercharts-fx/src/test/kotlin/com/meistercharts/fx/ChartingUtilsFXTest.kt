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
package com.meistercharts.fx

import assertk.*
import assertk.assertions.*
import it.neckar.events.MouseButton
import javafx.event.EventTarget
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.input.TouchEvent
import javafx.scene.input.TouchPoint
import org.junit.jupiter.api.Test

class ChartingUtilsFXTest {
  /**
   * The touch point that is released is *not* the one on the canvas.
   * Therefore, the remaining touch point on the canvas must be reported as target touch.
   */
  @Test
  fun testConvertEndKeepsRemainingTouchOnCanvas() {
    val canvas = Canvas()
    val otherNode = Canvas()

    val released = touchPoint(id = 1, state = TouchPoint.State.RELEASED, target = otherNode)
    val stillDown = touchPoint(id = 2, state = TouchPoint.State.MOVED, target = canvas)

    val converted = touchReleasedEvent(released, stillDown).convertEnd(canvas)

    assertThat(converted.touches.map { it.touchId.id }).containsExactly(2)
    assertThat(converted.changedTouches.map { it.touchId.id }).isEmpty()
    assertThat(converted.targetTouches.map { it.touchId.id }).containsExactly(2)
  }

  /**
   * The touch point that is released is the one on the canvas.
   * The remaining touch point belongs to another node and must therefore not be reported as target touch.
   */
  @Test
  fun testConvertEndSkipsRemainingTouchOnOtherNode() {
    val canvas = Canvas()
    val otherNode = Canvas()

    val released = touchPoint(id = 1, state = TouchPoint.State.RELEASED, target = canvas)
    val stillDown = touchPoint(id = 2, state = TouchPoint.State.MOVED, target = otherNode)

    val converted = touchReleasedEvent(released, stillDown).convertEnd(canvas)

    assertThat(converted.touches.map { it.touchId.id }).containsExactly(2)
    assertThat(converted.changedTouches.map { it.touchId.id }).containsExactly(1)
    assertThat(converted.targetTouches.map { it.touchId.id }).isEmpty()
  }

  /**
   * An entry is caused by no button at all, so [extractButton] throws on it - only the held-button flags answer which
   * button carries the gesture that is re-entering.
   */
  @Test
  fun testExtractHeldButtonReadsTheFlagsNotTheCause() {
    assertThat(mouseEnteredEvent(primaryButtonDown = true).extractHeldButton()).isEqualTo(MouseButton.Primary)
    assertThat(mouseEnteredEvent(secondaryButtonDown = true).extractHeldButton()).isEqualTo(MouseButton.Secondary)
    assertThat(mouseEnteredEvent(middleButtonDown = true).extractHeldButton()).isEqualTo(MouseButton.Middle)
  }

  @Test
  fun testExtractHeldButtonWithoutAnyButton() {
    assertThat(mouseEnteredEvent().extractHeldButton()).isNull()
  }

  private fun mouseEnteredEvent(
    primaryButtonDown: Boolean = false,
    middleButtonDown: Boolean = false,
    secondaryButtonDown: Boolean = false,
  ): MouseEvent {
    return MouseEvent(
      MouseEvent.MOUSE_ENTERED, 0.0, 0.0, 0.0, 0.0, javafx.scene.input.MouseButton.NONE, 0,
      false, false, false, false,
      primaryButtonDown, middleButtonDown, secondaryButtonDown,
      false, false, false, null
    )
  }

  private fun touchPoint(id: Int, state: TouchPoint.State, target: EventTarget): TouchPoint {
    return TouchPoint(id, state, 0.0, 0.0, 0.0, 0.0, target, null)
  }

  private fun touchReleasedEvent(vararg touchPoints: TouchPoint): TouchEvent {
    return TouchEvent(TouchEvent.TOUCH_RELEASED, touchPoints.first(), touchPoints.toList(), 1, false, false, false, false)
  }
}
