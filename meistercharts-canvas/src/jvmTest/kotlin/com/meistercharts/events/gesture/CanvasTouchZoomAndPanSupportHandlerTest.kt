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
package com.meistercharts.events.gesture

import assertk.*
import assertk.assertions.*
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.gesture.CanvasTouchZoomAndPanSupport.Handler
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Distance
import org.junit.jupiter.api.Test

class CanvasTouchZoomAndPanSupportHandlerTest {
  /**
   * Pins that addHandler/removeHandler form a proper add/remove pair.
   * Before the fix, removeHandler called add() — so removing a previously-registered
   * handler doubled it, leading to memory leaks and double-fired events.
   */
  @Test
  fun `removeHandler actually removes the handler`() {
    val support = CanvasTouchZoomAndPanSupport()
    val handler = NoopHandler()

    support.addHandler(handler)
    assertThat(support.handlers).containsExactly(handler)

    support.removeHandler(handler)
    assertThat(support.handlers).isEmpty()
  }

  @Test
  fun `removeHandler is idempotent on a handler that was not added`() {
    val support = CanvasTouchZoomAndPanSupport()
    support.removeHandler(NoopHandler())
    assertThat(support.handlers).isEmpty()
  }

  private class NoopHandler : Handler {
    override fun translate(oldCenter: Coordinates, newCenter: Coordinates, deltaCenter: Distance): EventConsumption =
      EventConsumption.Ignored

    override fun zoomChange(
      oldCenter: Coordinates,
      newCenter: Coordinates,
      oldDistanceBetweenTouches: Distance,
      newDistanceBetweenTouches: Distance,
      zoomFactorChangeX: Double,
      zoomFactorChangeY: Double,
    ): EventConsumption = EventConsumption.Ignored

    override fun doubleTap(tapLocation: Coordinates): EventConsumption = EventConsumption.Ignored
  }
}
