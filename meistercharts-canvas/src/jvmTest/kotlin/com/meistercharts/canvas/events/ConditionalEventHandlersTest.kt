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
package com.meistercharts.canvas.events

import assertk.*
import assertk.assertions.*
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.MockCanvas
import com.meistercharts.events.EventConsumption
import it.neckar.events.KeyCode
import it.neckar.events.KeyStroke
import it.neckar.events.KeyUpEvent
import it.neckar.events.MouseButton
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.geometry.Coordinates
import org.junit.jupiter.api.Test

class ConditionalEventHandlersTest {

  @Test
  fun `an enabled mouse handler sees the events`() {
    val recorder = RecordingMouseHandler()
    val handler = recorder.enabledWhen { true }

    assertThat(handler.onDown(MouseDownEvent(0.0, Coordinates.origin, MouseButton.Primary), NoChartSupport)).isEqualTo(EventConsumption.Consumed)
    assertThat(recorder.downCount).isEqualTo(1)
  }

  @Test
  fun `a disabled mouse handler sees nothing and ignores the event`() {
    val recorder = RecordingMouseHandler()
    val handler = recorder.enabledWhen { false }

    assertThat(handler.onDown(MouseDownEvent(0.0, Coordinates.origin, MouseButton.Primary), NoChartSupport)).isEqualTo(EventConsumption.Ignored)
    assertThat(handler.onMove(MouseMoveEvent(0.0, Coordinates.origin), NoChartSupport)).isEqualTo(EventConsumption.Ignored)
    assertThat(recorder.downCount).isEqualTo(0)
    assertThat(recorder.moveCount).isEqualTo(0)
  }

  @Test
  fun `the condition is asked for every event, not once`() {
    var enabled = false
    val recorder = RecordingMouseHandler()
    val handler = recorder.enabledWhen { enabled }

    assertThat(handler.onDown(MouseDownEvent(0.0, Coordinates.origin, MouseButton.Primary), NoChartSupport)).isEqualTo(EventConsumption.Ignored)
    enabled = true
    assertThat(handler.onDown(MouseDownEvent(0.0, Coordinates.origin, MouseButton.Primary), NoChartSupport)).isEqualTo(EventConsumption.Consumed)

    assertThat(recorder.downCount).isEqualTo(1)
  }

  @Test
  fun `a disabled key handler sees nothing and ignores the event`() {
    val recorder = RecordingKeyHandler()
    val handler = recorder.enabledWhen { false }

    assertThat(handler.onUp(KeyUpEvent(0.0, "a", KeyStroke(KeyCode('A'))), NoChartSupport)).isEqualTo(EventConsumption.Ignored)
    assertThat(recorder.upCount).isEqualTo(0)
  }

  @Test
  fun `an enabled key handler sees the events`() {
    val recorder = RecordingKeyHandler()
    val handler = recorder.enabledWhen { true }

    assertThat(handler.onUp(KeyUpEvent(0.0, "a", KeyStroke(KeyCode('A'))), NoChartSupport)).isEqualTo(EventConsumption.Consumed)
    assertThat(recorder.upCount).isEqualTo(1)
  }

  private class RecordingMouseHandler : CanvasMouseEventHandler {
    var downCount: Int = 0
    var moveCount: Int = 0

    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      downCount++
      return EventConsumption.Consumed
    }

    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      moveCount++
      return EventConsumption.Consumed
    }
  }

  private class RecordingKeyHandler : CanvasKeyEventHandler {
    var upCount: Int = 0

    override fun onUp(event: KeyUpEvent, chartSupport: ChartSupport): EventConsumption {
      upCount++
      return EventConsumption.Consumed
    }
  }

  private companion object {
    /**
     * The decorator only passes the chart support through - the tests never look at it.
     */
    val NoChartSupport: ChartSupport = ChartSupport(MockCanvas())
  }
}
