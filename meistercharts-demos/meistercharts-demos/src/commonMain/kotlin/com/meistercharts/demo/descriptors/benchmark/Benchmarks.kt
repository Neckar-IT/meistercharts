package com.meistercharts.demo.descriptors.benchmark

object Benchmarks {
  val benchmarkOperations: List<BenchmarkOperation> = buildList<BenchmarkOperation> {
    add(BenchmarkOperation("empty", 1) { _, _ -> })

    addAll(CanvasBasicsBenchmark.benchmarkOperations)
    addAll(PrimitivePaintingBenchmarks.benchmarkOperations)
    addAll(TextBenchmarks.benchmarkOperations)
    addAll(DataSeriesBenchmarks.benchmarkOperations)
  }
}
