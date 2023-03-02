package com.meistercharts.demo.descriptors.benchmark

import kotlin.time.Duration


data class BenchmarkResult(
  /**
   * A description of what is measured
   */
  val description: String,
  /**
   * The number of executions
   */
  val executionCount: Int,
  /**
   * How long took the invocation of the [CanvasRenderingContext] function
   */
  val duration: Duration,
  /**
   * How much time approximately passed for painting the operations
   */
  val frameTimestampDelta: Duration
)
