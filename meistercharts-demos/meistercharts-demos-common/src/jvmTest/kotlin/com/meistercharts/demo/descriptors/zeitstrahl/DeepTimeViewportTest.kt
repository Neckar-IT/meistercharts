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
package com.meistercharts.demo.descriptors.zeitstrahl

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class DeepTimeViewportTest {
  private fun viewport(): DeepTimeViewport = DeepTimeViewport(presentSeconds = present).apply { updateWindowWidth(1500.0) }

  @Test
  fun `the first width shows the Big Bang with a margin of 5 percent of the span`() {
    val viewport = viewport()

    assertThat(viewport.xOf(DeepTimeViewport.BigBangSeconds)).isCloseTo(1500.0 * 0.05 / 1.1, 1.0)
    assertThat(viewport.zoomLevel).isEqualTo(ZoomLevel(48))
  }

  @Test
  fun `zooming keeps the time below the pointer`() {
    val viewport = viewport()
    val seconds = viewport.secondsAt(400.0)

    repeat(200) { viewport.zoomAround(400.0, 0.8) }

    assertThat(viewport.xOf(seconds)).isCloseTo(400.0, 1.0)
  }

  @Test
  fun `zooming in stops at one day across the window`() {
    val viewport = viewport()

    repeat(500) { viewport.zoomAround(750.0, 0.5) }

    assertThat(viewport.visibleTo - viewport.visibleFrom).isCloseTo(ProlepticGregorianCalendar.SecondsPerDay, 1.0)
    assertThat(viewport.zoomLevel).isEqualTo(ZoomLevel(5))
  }

  @Test
  fun `panning moves the time by the dragged pixels`() {
    val viewport = viewport()
    viewport.showWindow(0.0, 1500.0 * 86_400.0)

    viewport.panBy(100.0)

    assertThat(viewport.visibleFrom).isCloseTo(-100.0 * 86_400.0, 1.0)
  }

  private companion object {
    val present: Double = ProlepticGregorianCalendar.secondsOf(CivilDate(2026, 11, 16))
  }
}
