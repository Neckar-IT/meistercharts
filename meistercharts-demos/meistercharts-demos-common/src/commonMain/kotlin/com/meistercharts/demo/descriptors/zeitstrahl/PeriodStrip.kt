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


/**
 * The rules of the period strip from `internal/personal/zeitstrahl/KONZEPT.md`, section "Epochen-Leiste", without drawing.
 */
object PeriodStrip {
  private const val MinMedianWidth: Double = 40.0

  fun periodizationId(viewport: DeepTimeViewport): PeriodizationId {
    return if (DeepTimeFormat.isDeepTimeWindow(viewport.centerSeconds, viewport.visibleSeconds)) PeriodizationId.Ics else PeriodizationId.EuropeHistory
  }

  fun layout(sections: List<ZeitstrahlPocSection>, viewport: DeepTimeViewport): PeriodStripLayout {
    val candidates = sections.filter { it.end - it.start < viewport.zoomLevel.maxDurationSeconds }
    val visibleByLevel = candidates
      .filter { viewport.overlaps(it.start, it.end) }
      .groupBy { it.level }
    val rowLevels = visibleByLevel
      .filter { (_, levelSections) -> levelSections.size >= 2 && median(levelSections.map { viewport.visibleXRange(it.start, it.end).width() }) >= MinMedianWidth }
      .keys.sorted().takeLast(ZeitstrahlPocLayout.StripRowCount)
    val coarsestRowLevel = rowLevels.minOrNull() ?: Int.MAX_VALUE
    val breadcrumbs = candidates
      .filter { it.level < coarsestRowLevel && it.start <= viewport.centerSeconds && viewport.centerSeconds < it.end }
      .sortedBy { it.level }
    return PeriodStripLayout(rowLevels.map { level -> visibleByLevel.getValue(level).sortedBy { it.start } }, breadcrumbs)
  }

  private fun ClosedFloatingPointRange<Double>.width(): Double = endInclusive - start

  private fun median(values: List<Double>): Double {
    val sorted = values.sorted()
    val middle = sorted.size / 2
    return if (sorted.size % 2 == 0) (sorted[middle - 1] + sorted[middle]) / 2 else sorted[middle]
  }
}
