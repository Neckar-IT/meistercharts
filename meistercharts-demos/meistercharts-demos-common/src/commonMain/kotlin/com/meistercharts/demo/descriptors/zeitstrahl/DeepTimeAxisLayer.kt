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
 * The row of the path with the date of the window center, below it the deep time axis, and a grid line per tick down to the period strip.
 * [ZeitstrahlPocLanesLayer] writes the path into the left of the path row.
 */
class DeepTimeAxisLayer(private val viewport: DeepTimeViewport) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val step = DeepTimeTicks.step(viewport.secondsPerPixel)
    val ticks = DeepTimeTicks.ticks(viewport.visibleFrom, viewport.visibleTo, viewport.centerSeconds, step)
    val axisTop = ZeitstrahlPocLayout.PathHeight
    val axisBottom = ZeitstrahlPocLayout.LanesTop
    val gridBottom = gc.height - ZeitstrahlPocLayout.StripHeight

    gc.fill(SignalColors.ground)
    gc.fillRect(0.0, 0.0, gc.width, gc.height)

    gc.font(FontDescriptorFragment(size = FontSize(14.0), weight = FontWeight.Bold))
    gc.fill(SignalColors.ink)
    gc.fillText(DeepTimeTicks.centerLabel(viewport.centerSeconds, viewport.visibleSeconds, step), gc.width - 16.0, axisTop / 2 - 8.0, Direction.CenterRight)
    gc.font(FontDescriptorFragment(size = FontSize(11.0), weight = FontWeight.Normal))
    gc.fill(SignalColors.muted)
    gc.fillText("Fenster ${DeepTimeFormat.duration(viewport.visibleSeconds)} · Zoomstufe ${viewport.zoomLevel}", gc.width - 16.0, axisTop / 2 + 9.0, Direction.CenterRight)

    gc.font(FontDescriptorFragment(size = FontSize(10.5), weight = FontWeight.Normal))
    gc.fill(SignalColors.muted)
    gc.fillText("STRUKTUR", 14.0, axisBottom - 8.0, Direction.BottomLeft)

    gc.lineWidth = 1.0
    gc.strokeStyle(SignalColors.line)
    gc.strokeLine(0.0, axisTop, gc.width, axisTop)
    gc.strokeLine(0.0, axisBottom, gc.width, axisBottom)
    gc.strokeLine(ZeitstrahlPocLayout.TreeWidth, axisTop, ZeitstrahlPocLayout.TreeWidth, gridBottom)

    gc.saved {
      gc.translate(ZeitstrahlPocLayout.TreeWidth, 0.0)
      gc.clip(0.0, axisTop, viewport.windowWidth, gridBottom - axisTop)
      gc.font(FontDescriptorFragment(size = FontSize(11.0), weight = FontWeight.Normal))
      ticks.forEach { tick ->
        val x = viewport.xOf(tick.seconds)
        gc.strokeStyle(SignalColors.gridMajor)
        gc.strokeLine(x, axisBottom, x, gridBottom)
        gc.strokeStyle(SignalColors.tickMark)
        gc.strokeLine(x, axisBottom - 8.0, x, axisBottom)
        gc.fill(SignalColors.ink)
        gc.fillText(tick.label, x + 3.0, axisBottom - 3.0, Direction.BottomLeft)
      }
    }
  }
}
