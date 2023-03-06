/**
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
package com.meistercharts.demo.descriptors.benchmark

import com.meistercharts.algorithms.layers.LayerPaintingContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration


/**
 * An operation that is run in the benchmark
 */
class BenchmarkOperation(
  val description: String,
  val executionCount: Int,
  val operation: (LayerPaintingContext, Int) -> Unit
) {

  fun measureTime(paintingContext: LayerPaintingContext): BenchmarkResult {
    val frameTimestampDelta = paintingContext.frameTimestampDelta.toDuration(DurationUnit.MILLISECONDS)
    return kotlin.time.measureTime {
      operation(paintingContext, executionCount)
    }.let {
      BenchmarkResult(description, executionCount, it, frameTimestampDelta)
    }
  }
}
