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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.strokeStyle
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontWeight
import it.neckar.geometry.Direction

/**
 * The period strip at the bottom, right of the tree column: sections of `ics` or `europe-history` after the rules of [PeriodStrip].
 */
class PeriodStripLayer(
  private val viewport: DeepTimeViewport,
  private val loader: WindowLoader<PeriodizationId, ZeitstrahlPocSection>,
) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val stripTop = gc.height - ZeitstrahlPocLayout.StripHeight
    val periodizationId = PeriodStrip.periodizationId(viewport)
    loader.ensureLoaded(periodizationId, viewport.visibleFrom, viewport.visibleTo)

    gc.fill(SignalColors.ground)
    gc.fillRect(0.0, stripTop, gc.width, ZeitstrahlPocLayout.StripHeight)
    gc.strokeStyle(SignalColors.line)
    gc.lineWidth = 1.0
    gc.strokeLine(0.0, stripTop, gc.width, stripTop)

    val sections = if (loader.loadedKey == periodizationId) loader.items else emptyList()
    val layout = PeriodStrip.layout(sections, viewport)

    gc.font(FontDescriptorFragment(size = FontSize(11.0), weight = FontWeight.Bold))
    gc.fill(SignalColors.muted)
    gc.fillText(listOf(periodizationId.value).plus(layout.breadcrumbs.map { it.title }).joinToString(" › "), 14.0, stripTop + 3.0, Direction.TopLeft)
    loader.failure?.let { failure ->
      gc.fill(SignalColors.failure)
      gc.fillText(serverFailureLabel(failure), gc.width - 6.0, stripTop + 3.0, Direction.TopRight)
    }

    gc.saved {
      gc.translate(ZeitstrahlPocLayout.TreeWidth, 0.0)
      gc.clip(0.0, stripTop, viewport.windowWidth, ZeitstrahlPocLayout.StripHeight)
      gc.font(FontDescriptorFragment(size = FontSize(11.0), weight = FontWeight.Normal))
      layout.rows.forEachIndexed { rowIndex, row ->
        val rowTop = stripTop + ZeitstrahlPocLayout.StripBreadcrumbHeight + rowIndex * ZeitstrahlPocLayout.StripRowHeight
        row.forEachIndexed { sectionIndex, section ->
          val visibleX = viewport.visibleXRange(section.start, section.end)
          val left = visibleX.start
          val right = visibleX.endInclusive
          gc.fill(if (sectionIndex % 2 == 0) SignalColors.surface else SignalColors.surfaceRaised)
          gc.fillRect(left, rowTop, right - left, ZeitstrahlPocLayout.StripSectionHeight)
          val label = continuedLabel(section.title, viewport.xOf(section.start))
          if (gc.calculateTextWidth(label) < right - left - 4.0) {
            gc.fill(SignalColors.inkSoft)
            gc.fillText(label, (left + right) / 2, rowTop + (ZeitstrahlPocLayout.StripSectionHeight) / 2, Direction.Center)
          }
        }
      }
    }
  }
}
