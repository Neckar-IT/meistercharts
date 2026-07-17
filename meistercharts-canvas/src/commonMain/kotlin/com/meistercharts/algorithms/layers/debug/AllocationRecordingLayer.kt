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
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.allocation.AllocationRecordingEngine
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.color.Color
import com.meistercharts.style.BoxStyle
import it.neckar.geometry.Direction

/**
 * Overlay that shows the current allocation recording report (top allocated types per layer).
 *
 * Only paints while [AllocationRecordingEngine.mode] is a recording mode - toggle it with
 * Ctrl+Shift+Alt+A ([ToggleDebuggingModeLayer]). JVM only; on JS the report is always empty.
 */
class AllocationRecordingLayer(
  val configuration: Configuration = Configuration(),
) : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val mode = AllocationRecordingEngine.mode
    if (mode.recording.not()) {
      return
    }

    val report = AllocationRecordingEngine.currentReport()

    val lines = buildList {
      add("Allocations · JFR sampled · $mode")
      if (report.layerAllocations.isEmpty()) {
        add("(no samples yet - keep interacting)")
      } else {
        add("${report.totalSamples} samples")
        report.layerAllocations.take(configuration.maxLayers).forEach { layerAllocations ->
          add("[${layerAllocations.layerName}] ${layerAllocations.totalSamples}")
          layerAllocations.allocationsByType.take(configuration.maxTypesPerLayer).forEach { type ->
            add("  ${type.samples}× ${shortTypeName(type.typeName)}")
          }
        }
      }
    }

    val gc = paintingContext.gc
    gc.saved {
      gc.translate(gc.width, 0.0)
      gc.paintTextBox(
        lines = lines,
        anchorDirection = Direction.TopRight,
        anchorGapHorizontal = configuration.gap,
        anchorGapVertical = configuration.gap,
        boxStyle = configuration.boxStyle,
        textColor = configuration.textColor,
      )
    }
  }

  private fun shortTypeName(typeName: String): String {
    return if (typeName.contains('.')) typeName.substringAfterLast('.') else typeName
  }

  class Configuration {
    /**
     * The number of layers shown (the ones with the most samples)
     */
    var maxLayers: Int = 6

    /**
     * The number of types shown per layer (the ones with the most samples)
     */
    var maxTypesPerLayer: Int = 4

    var gap: Double = 10.0
    var boxStyle: BoxStyle = BoxStyle.gray
    var textColor: Color = Color.web("#333333")
  }
}
