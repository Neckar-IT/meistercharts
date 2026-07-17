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
package com.meistercharts.canvas.allocation

/**
 * Formats the report as a readable multi-line string: layer -> type -> the top stacktraces (with the
 * number of samples at each). Meant for dumping to the log/console for offline analysis - the overlay
 * cannot show full stacktraces.
 */
fun AllocationReport.format(
  maxTypesPerLayer: Int = 20,
  maxStacktracesPerType: Int = 5,
): String {
  if (layerAllocations.isEmpty()) {
    return "Allocation report: empty (recording is off or no samples yet)"
  }

  return buildString {
    appendLine("=== Allocation report - $totalSamples samples, ~$estimatedBytes bytes (JFR sampled) ===")
    layerAllocations.forEach { layer ->
      appendLine("[${layer.layerName}] ${layer.totalSamples} samples, ~${layer.estimatedBytes} bytes")

      layer.allocationsByType.take(maxTypesPerLayer).forEach { type ->
        appendLine("  ${type.samples}x ${type.typeName} (~${type.estimatedBytes} bytes)")

        type.stacktraces.entries
          .sortedByDescending { it.value }
          .take(maxStacktracesPerType)
          .forEach { (stacktrace, count) ->
            appendLine("      ${count}x @")
            stacktrace.split("\n").forEach { frame ->
              appendLine("          $frame")
            }
          }
      }
    }
  }
}
