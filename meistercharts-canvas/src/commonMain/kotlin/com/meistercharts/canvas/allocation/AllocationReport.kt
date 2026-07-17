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
 * A snapshot of the allocations that were sampled during layer paints, broken down by layer and type.
 *
 * The counts are JFR *samples*, not exact allocation counts - they show what is allocated and where,
 * in relative volume, not an exact per-frame number. See [AllocationRecordingEngine].
 */
data class AllocationReport(
  /**
   * The allocations per layer, sorted by sample count (descending)
   */
  val layerAllocations: List<LayerAllocations>,
) {
  /**
   * The total number of samples across all layers
   */
  val totalSamples: Int
    get() = layerAllocations.sumOf { it.totalSamples }

  /**
   * The total estimated number of allocated bytes across all layers
   */
  val estimatedBytes: Long
    get() = layerAllocations.sumOf { it.estimatedBytes }

  /**
   * The layer with the most allocation samples
   */
  val worst: LayerAllocations?
    get() = layerAllocations.maxByOrNull { it.totalSamples }

  companion object {
    val empty: AllocationReport = AllocationReport(emptyList())
  }
}

/**
 * The sampled allocations of a single layer, broken down by type.
 */
data class LayerAllocations(
  /**
   * The name of the layer the allocations were attributed to (from the sample stacktrace)
   */
  val layerName: String,
  /**
   * The allocations of this layer, one entry per type, sorted by sample count (descending)
   */
  val allocationsByType: List<TypeAllocation>,
) {
  /**
   * The total number of samples of this layer
   */
  val totalSamples: Int
    get() = allocationsByType.sumOf { it.samples }

  /**
   * The total estimated number of allocated bytes of this layer
   */
  val estimatedBytes: Long
    get() = allocationsByType.sumOf { it.estimatedBytes }
}

/**
 * The sampled allocations of a single type within one layer.
 */
data class TypeAllocation(
  /**
   * The name of the allocated type (e.g. `com.meistercharts.model.Insets`, `[D` for `double[]`)
   */
  val typeName: String,
  /**
   * How often the type was sampled
   */
  val samples: Int,
  /**
   * The estimated number of bytes allocated for this type (sum of the JFR sample weights)
   */
  val estimatedBytes: Long,
  /**
   * The captured stacktraces (compact, top frames) mapped to the number of samples at each
   * stacktrace. Empty unless the recording mode is [AllocationRecordingMode.ByTypeAndStacktrace].
   */
  val stacktraces: Map<String, Int> = emptyMap(),
)
