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
