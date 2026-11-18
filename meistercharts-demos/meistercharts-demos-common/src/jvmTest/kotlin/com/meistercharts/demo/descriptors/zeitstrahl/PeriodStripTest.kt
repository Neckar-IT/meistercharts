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
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class PeriodStripTest {
  private fun millionYearsAgo(millionYears: Double): Double = DeepTimeFormat.secondsOfYearsBeforePresent(millionYears * 1e6)

  private fun section(title: String, level: Int, from: Double, to: Double): ZeitstrahlPocSection = ZeitstrahlPocSection(EntryId(title), title, level, millionYearsAgo(from), millionYearsAgo(to))

  /** Boundaries of the ICS chart 2023/09 in million years. */
  private val ics: List<ZeitstrahlPocSection> = listOf(
    section("Phanerozoikum", 0, 538.8, 0.0),
    section("Mesozoikum", 1, 251.9, 66.0),
    section("Känozoikum", 1, 66.0, 0.0),
    section("Kreide", 2, 145.0, 66.0),
    section("Paläogen", 2, 66.0, 23.03),
    section("Oberkreide", 3, 100.5, 66.0),
    section("Paläozän", 3, 66.0, 56.0),
    section("Campanium", 4, 83.6, 72.1),
    section("Maastrichtium", 4, 72.1, 66.0),
    section("Danium", 4, 66.0, 61.6),
    section("Selandium", 4, 61.6, 59.2),
    section("Thanetium", 4, 59.2, 56.0),
  )

  private fun layoutFor(from: Double, to: Double, sections: List<ZeitstrahlPocSection> = ics): PeriodStripLayout {
    val viewport = DeepTimeViewport(presentSeconds = 1.8e9).apply {
      updateWindowWidth(1500.0)
      showWindow(millionYearsAgo(from), millionYearsAgo(to))
    }
    return PeriodStrip.layout(sections, viewport)
  }

  @Test
  fun `the example of the concept shows periods, epochs and ages below the breadcrumb Kaenozoikum`() {
    val layout = layoutFor(73.0, 57.0)

    assertThat(layout.rows.map { row -> row.map { it.title } }).containsExactly(
      listOf("Kreide", "Paläogen"),
      listOf("Oberkreide", "Paläozän"),
      listOf("Campanium", "Maastrichtium", "Danium", "Selandium", "Thanetium"),
    )
    assertThat(layout.breadcrumbs.map { it.title }).containsExactly("Känozoikum")
  }

  @Test
  fun `four qualifying levels leave the coarsest one to the breadcrumbs`() {
    val substages = listOf(section("Danium früh", 5, 66.0, 63.8), section("Danium spät", 5, 63.8, 61.6), section("Selandium früh", 5, 61.6, 60.4))
    val layout = layoutFor(73.0, 57.0, ics + substages)

    assertThat(layout.rows.map { row -> row.first().level }).containsExactly(3, 4, 5)
    assertThat(layout.breadcrumbs.map { it.title }).containsExactly("Känozoikum", "Paläogen")
  }

  @Test
  fun `a level whose median section is narrower than 40 px stays out`() {
    // 0.3 million years are 28 px in a window of 16 million years on 1500 px.
    val substages = listOf(section("A", 5, 65.0, 64.7), section("B", 5, 64.7, 64.4), section("C", 5, 64.4, 60.0))
    val layout = layoutFor(73.0, 57.0, ics + substages)

    assertThat(layout.rows.map { row -> row.first().level }).containsExactly(2, 3, 4)
  }

  @Test
  fun `the strip shows ics before 10,000 BC and for windows over 50,000 years, else europe-history`() {
    fun periodizationIdFor(from: CivilDate, to: CivilDate): PeriodizationId = PeriodStrip.periodizationId(DeepTimeViewport(presentSeconds = 1.8e9).apply {
      updateWindowWidth(1500.0)
      showWindow(ProlepticGregorianCalendar.secondsOf(from), ProlepticGregorianCalendar.secondsOf(to))
    })

    assertThat(periodizationIdFor(CivilDate(-12_000, 1, 1), CivilDate(-11_000, 1, 1))).isEqualTo(PeriodizationId.Ics)
    assertThat(periodizationIdFor(CivilDate(-28_000, 1, 1), CivilDate(30_000, 1, 1))).isEqualTo(PeriodizationId.Ics)
    assertThat(periodizationIdFor(CivilDate(-20_000, 1, 1), CivilDate(10_000, 1, 1))).isEqualTo(PeriodizationId.EuropeHistory)
    assertThat(periodizationIdFor(CivilDate(1785, 1, 1), CivilDate(1800, 1, 1))).isEqualTo(PeriodizationId.EuropeHistory)
  }

  @Test
  fun `a level qualifies by its median section, a narrow first section does not keep it out`() {
    // 0.2, 0.7 and 0.7 million years are 19, 66 and 66 px wide.
    val substages = listOf(section("A", 5, 65.0, 64.8), section("B", 5, 64.8, 64.1), section("C", 5, 64.1, 63.4))
    val layout = layoutFor(73.0, 57.0, ics + substages)

    assertThat(layout.rows.map { row -> row.first().level }).containsExactly(3, 4, 5)
  }

  @Test
  fun `a window inside the Maastrichtium drops every section of 16 tiles and more`() {
    val layout = layoutFor(69.5, 68.5)

    assertThat(layout.rows).containsExactly()
    assertThat(layout.breadcrumbs.map { it.title }).containsExactly("Maastrichtium")
  }
}
