package com.meistercharts.demo.descriptors.benchmark

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.model.Direction
import it.neckar.open.formatting.decimalFormat
import com.meistercharts.style.BoxStyle
import it.neckar.logging.LoggerFactory
import kotlin.time.DurationUnit

/**
 * Executes the benchmarks
 */
class BenchmarkLayer(
  val benchmarkOperations: List<BenchmarkOperation> = Benchmarks.benchmarkOperations
) : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Content


  val benchmarkResults = mutableListOf<BenchmarkResult>()

  var operationIndex = 0
  var isFirstPaint = true

  override fun paint(paintingContext: LayerPaintingContext) {
    val layerSupport = paintingContext.layerSupport

    if (operationIndex == benchmarkOperations.size) {
      operationIndex = 0
      isFirstPaint = true
      dumpBenchmarkResults(paintingContext.gc)
      benchmarkResults.clear()
      return
    }

    // trigger next paint
    layerSupport.markAsDirty()

    // Skip the first paint because it does not provide a frameTimestampDelta that reflects the time the painting took
    if (isFirstPaint) {
      isFirstPaint = false
      return
    }

    val benchmarkOperation = benchmarkOperations[operationIndex]

    // Show some progress information to the user
    val benchmarkProgressMessage = "benchmark <${benchmarkOperation.description}> in progress... ${operationIndex + 1}/${benchmarkOperations.size}"
    logger.info(benchmarkProgressMessage)
    paintingContext.gc.paintTextBox(
      benchmarkProgressMessage,
      Direction.TopLeft,
      10.0,
      10.0,
      BoxStyle.none,
      Color.black
    )
    val result = benchmarkOperation.measureTime(paintingContext)
    benchmarkResults.add(result)
    logger.info("benchmark <${benchmarkOperation.description}> finished: ${result.description} -  ${result.duration.toDouble(DurationUnit.MILLISECONDS)} ms, painting: ${result.frameTimestampDelta.toDouble(DurationUnit.MILLISECONDS)} ms")
    operationIndex++
  }

  private fun dumpBenchmarkResults(gc: CanvasRenderingContext) {
    gc.saved {
      benchmarkResults.forEach {
        val text = "${it.description} @${it.executionCount}: operation took ${decimalFormat.format(it.duration.toDouble(DurationUnit.MILLISECONDS))} ms, painting took ${decimalFormat.format(it.frameTimestampDelta.toDouble(DurationUnit.MILLISECONDS))} ms"
        logger.debug(text)
        gc.translate(0.0, 20.0)
        gc.paintTextBox(text, Direction.TopLeft, 10.0, 10.0, BoxStyle.none, Color.black)
      }
    }
    logger.info("-------------------------------")
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.benchmark.BenchmarkLayer")
  }
}
