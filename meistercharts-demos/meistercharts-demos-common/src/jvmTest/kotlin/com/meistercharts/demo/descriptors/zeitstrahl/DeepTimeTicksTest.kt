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
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class DeepTimeTicksTest {
  private fun labels(viewport: DeepTimeViewport): List<String> {
    val step = DeepTimeTicks.step(viewport.secondsPerPixel)
    return DeepTimeTicks.ticks(viewport.visibleFrom, viewport.visibleTo, viewport.centerSeconds, step).map { it.label }
  }

  private fun viewportShowing(from: CivilDate, to: CivilDate): DeepTimeViewport = DeepTimeViewport(presentSeconds = present).apply {
    updateWindowWidth(1500.0)
    showWindow(ProlepticGregorianCalendar.secondsOf(from), ProlepticGregorianCalendar.secondsOf(to))
  }

  @Test
  fun `the whole universe shows billions of years`() {
    val viewport = DeepTimeViewport(presentSeconds = present).apply { updateWindowWidth(1500.0) }

    assertThat(DeepTimeTicks.step(viewport.secondsPerPixel)).isEqualTo(DeepTimeTickStep.Years(2_000_000_000))
    assertThat(labels(viewport)).contains("vor 12 Mrd. Jahren")
  }

  @Test
  fun `a window before 10,000 BC counts years before present`() {
    val viewport = viewportShowing(CivilDate(-30_000, 1, 1), CivilDate(-20_000, 1, 1))

    assertThat(DeepTimeFormat.isDeepTimeWindow(viewport.centerSeconds, viewport.visibleTo - viewport.visibleFrom)).isTrue()
    assertThat(labels(viewport)).contains("vor 25.000 Jahren")
  }

  @Test
  fun `a window around Caesar counts historical years`() {
    val viewport = viewportShowing(CivilDate(-50, 1, 1), CivilDate(-40, 1, 1))

    assertThat(DeepTimeFormat.isDeepTimeWindow(viewport.centerSeconds, viewport.visibleTo - viewport.visibleFrom)).isFalse()
    assertThat(labels(viewport)).contains("44 v. Chr.")
  }

  @Test
  fun `steps of years stay round on both sides of the missing year 0`() {
    assertThat(labels(viewportShowing(CivilDate(-60, 1, 1), CivilDate(30, 1, 1)))).containsExactly("60 v. Chr.", "50 v. Chr.", "40 v. Chr.", "30 v. Chr.", "20 v. Chr.", "10 v. Chr.", "10", "20", "30")
  }

  @Test
  fun `a window of ten days labels every day`() {
    assertThat(labels(viewportShowing(CivilDate(1789, 7, 10), CivilDate(1789, 7, 20)))).containsExactly(
      "10. Juli 1789", "11. Juli 1789", "12. Juli 1789", "13. Juli 1789", "14. Juli 1789", "15. Juli 1789",
      "16. Juli 1789", "17. Juli 1789", "18. Juli 1789", "19. Juli 1789", "20. Juli 1789",
    )
  }

  @Test
  fun `steps of five days keep a full step before the 1st of March`() {
    val ticks = DeepTimeTicks.ticks(
      ProlepticGregorianCalendar.secondsOf(CivilDate(1789, 2, 1)),
      ProlepticGregorianCalendar.secondsOf(CivilDate(1789, 3, 1)),
      ProlepticGregorianCalendar.secondsOf(CivilDate(1789, 2, 15)),
      DeepTimeTickStep.Days(5),
    )

    assertThat(ticks.map { it.label }).containsExactly("1. Feb. 1789", "6. Feb. 1789", "11. Feb. 1789", "16. Feb. 1789", "21. Feb. 1789", "1. März 1789")
  }

  @Test
  fun `deep time ends at 1950 when the window reaches into the future`() {
    val viewport = DeepTimeViewport(presentSeconds = present).apply {
      updateWindowWidth(1500.0)
      panBy(-1e9)
    }

    assertThat(labels(viewport).last()).isEqualTo("1950")
    assertThat(DeepTimeTicks.centerLabel(viewport.centerSeconds, viewport.visibleTo - viewport.visibleFrom, DeepTimeTicks.step(viewport.secondsPerPixel))).isEqualTo("2026")
  }

  @Test
  fun `a window of one day labels hours`() {
    val viewport = viewportShowing(CivilDate(1789, 7, 14), CivilDate(1789, 7, 15))

    assertThat(DeepTimeTicks.step(viewport.secondsPerPixel)).isEqualTo(DeepTimeTickStep.Hours(3))
    assertThat(labels(viewport)).contains("06:00")
    assertThat(labels(viewport).first()).isEqualTo("14. Juli 1789")
  }

  @Test
  fun `one day after the Big Bang labels exact hours`() {
    val viewport = DeepTimeViewport(presentSeconds = present).apply {
      updateWindowWidth(1500.0)
      showWindow(DeepTimeViewport.BigBangSeconds, DeepTimeViewport.BigBangSeconds + ProlepticGregorianCalendar.SecondsPerDay)
    }

    assertThat(labels(viewport).filter { it.endsWith(":00") }).containsExactly("03:00", "06:00", "09:00", "12:00", "15:00", "18:00", "21:00")
  }

  @Test
  fun `the center label follows the step`() {
    val seconds = ProlepticGregorianCalendar.secondsOf(CivilDate(1789, 7, 14))

    val century = 100 * ProlepticGregorianCalendar.SecondsPerYear

    assertThat(DeepTimeTicks.centerLabel(seconds, century, DeepTimeTickStep.Years(10))).isEqualTo("1789")
    assertThat(DeepTimeTicks.centerLabel(seconds, century, DeepTimeTickStep.Months(1))).isEqualTo("Juli 1789")
    assertThat(DeepTimeTicks.centerLabel(seconds, century, DeepTimeTickStep.Hours(1))).isEqualTo("14. Juli 1789")
    assertThat(DeepTimeTicks.centerLabel(DeepTimeViewport.BigBangSeconds, 1e10 * ProlepticGregorianCalendar.SecondsPerYear, DeepTimeTickStep.Years(1_000_000_000))).isEqualTo("vor 13,8 Mrd. Jahren")
  }

  private companion object {
    val present: Double = ProlepticGregorianCalendar.secondsOf(CivilDate(2026, 11, 16))
  }
}
