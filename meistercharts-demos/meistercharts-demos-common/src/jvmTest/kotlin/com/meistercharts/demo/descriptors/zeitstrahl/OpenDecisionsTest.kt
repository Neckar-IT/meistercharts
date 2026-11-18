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
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class OpenDecisionsTest {
  private val revolution = EntryId("Q6534")

  /** One paint that shows [revolution] at [share] of the window width. */
  private fun OpenDecisions.paint(share: Double): Boolean = update(revolution, share).also { endPass() }

  private fun openDecisionsAfterPaint(share: Double): OpenDecisions = OpenDecisions().also { val _ = it.paint(share) }

  @Test
  fun `opens at 60 percent of the window and closes below 40 percent`() {
    val decisions = OpenDecisions()

    assertThat(decisions.paint(0.59)).isFalse()
    assertThat(decisions.paint(0.6)).isTrue()
    assertThat(decisions.paint(0.4)).isTrue()
    assertThat(decisions.paint(0.39)).isFalse()
  }

  @Test
  fun `keeps its state between 40 and 60 percent`() {
    val decisions = OpenDecisions()

    assertThat(decisions.paint(0.5)).isFalse()
    assertThat(decisions.paint(1.0)).isTrue()
    assertThat(decisions.paint(0.5)).isTrue()
  }

  @Test
  fun `a click opens a small period and keeps it open while it stays in the window`() {
    val decisions = openDecisionsAfterPaint(0.1)

    decisions.toggle(revolution)

    assertThat(decisions.paint(0.1)).isTrue()
    assertThat(decisions.paint(0.01)).isTrue()
  }

  @Test
  fun `a click closes a period that fills the window`() {
    val decisions = openDecisionsAfterPaint(1.0)

    decisions.toggle(revolution)

    assertThat(decisions.paint(1.0)).isFalse()
  }

  @Test
  fun `the click ends once the period leaves the window`() {
    val decisions = openDecisionsAfterPaint(1.0)
    decisions.toggle(revolution)

    // A paint without the period: it left the window.
    decisions.endPass()

    assertThat(decisions.paint(1.0)).isTrue()
  }
}
