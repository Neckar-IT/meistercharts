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
import com.meistercharts.canvas.ArcType
import com.meistercharts.canvas.saved
import com.meistercharts.model.Size
import com.meistercharts.resources.Icons
import com.meistercharts.style.Palette.getChartColor

/**
 * Benchmarks for primitive painting operations
 */
object PrimitivePaintingBenchmarks {

  val benchmarkOperations: List<BenchmarkOperation> = listOf(
    BenchmarkOperation("paintImage", 10_000, this::paintImage),
    BenchmarkOperation("strokeStyle", 100_000, this::strokeStyle),
    BenchmarkOperation("strokeLine", 100_000, this::strokeLine),
    BenchmarkOperation("strokeRect", 100_000, this::strokeRect),
    BenchmarkOperation("strokeOval", 100_000, this::strokeOval),
    BenchmarkOperation("strokeArc", 100_000, this::strokeArc),
    BenchmarkOperation("fillStyle", 100_000, this::fillStyle),
    BenchmarkOperation("fillRect", 100_000, this::fillRect),
    BenchmarkOperation("fillOval", 100_000, this::fillOval),
    BenchmarkOperation("fillArc", 1_000, this::fillArc),
    BenchmarkOperation("setLineDash", 10_000, this::setLineDash),
    BenchmarkOperation("path", 100, this::path)
  )

  private fun path(paintingContext: LayerPaintingContext, executionCount: Int) {
    val gc = paintingContext.gc
    gc.saved {
      gc.stroke(getChartColor(0))
      for (i in 0 until executionCount) {
        gc.beginPath()
        gc.arcCenter(i * 0.1, i * 0.2, i * 0.3, i * 0.4, i * 0.5)
        gc.bezierCurveTo(i * 0.1, i * 0.2, i * 0.3, i * 0.4, i * 0.5, i * 0.6)
        gc.quadraticCurveTo(i * 0.1, i * 0.2, i * 0.3, i * 0.4)
        gc.lineTo(i * 0.1, i * 0.2)
        gc.stroke()
      }
    }
  }

  private fun paintImage(paintingContext: LayerPaintingContext, executionCount: Int) {
    val paintable = Icons.mapMarker(Size.PX_120, getChartColor(12))

    val gc = paintingContext.gc
    for (i in 0 until executionCount) {
      gc.saved {
        paintable.paint(paintingContext, 200.0 + i * 0.01, 200.0 + i * 0.02)
      }
    }
  }

  private fun strokeLine(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.stroke(getChartColor(13))
    for (i in 0 until executionCount) {
      val doubleValue = i * 0.1
      paintingContext.gc.strokeLine(200.0 + doubleValue, 200.0 + doubleValue, 300.0 - doubleValue, 300.0 + doubleValue)
    }
  }

  private fun fillStyle(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      for (i in 0 until executionCount) {
        paintingContext.gc.fillStyle(getChartColor(i))
      }
    }
  }


  private fun strokeStyle(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      for (i in 0 until executionCount) {
        paintingContext.gc.strokeStyle(getChartColor(i))
      }
    }
  }

  private fun setLineDash(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      for (i in 0 until executionCount) {
        paintingContext.gc.setLineDash(i * 0.1, i * 0.2)
      }
    }
  }

  private fun fillArc(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      paintingContext.gc.fillStyle(getChartColor(4))
      for (i in 0 until executionCount) {
        paintingContext.gc.fillArcCenter(0.0, 0.0, 100.0, 0.5, 0.5, ArcType.Round)
      }
    }
  }

  private fun fillOval(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      paintingContext.gc.fillStyle(getChartColor(5))
      for (i in 0 until executionCount) {
        val doubleValue = i * 0.01
        paintingContext.gc.fillOvalCenter(200.0 + doubleValue, 200.0 + doubleValue, doubleValue, doubleValue)
      }
    }
  }

  private fun fillRect(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      paintingContext.gc.fillStyle(getChartColor(6))
      for (i in 0 until executionCount) {
        val doubleValue = i * 0.01
        paintingContext.gc.fillRect(0.0 + doubleValue, 0.0 + doubleValue, doubleValue, doubleValue)
      }
    }
  }


  private fun strokeArc(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      paintingContext.gc.strokeStyle(getChartColor(9))
      for (i in 0 until executionCount) {
        paintingContext.gc.strokeArcCenter(200.0, 200.0, 100.0, 0.5, 0.5, ArcType.Round)
      }
    }
  }

  private fun strokeOval(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      paintingContext.gc.strokeStyle(getChartColor(10))
      for (i in 0 until executionCount) {
        paintingContext.gc.strokeOvalCenter(200.0 + i, 200.0 + i, i * 0.1, i * 0.15)
      }
    }
  }

  private fun strokeRect(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      paintingContext.gc.strokeStyle(getChartColor(11))
      for (i in 0 until executionCount) {
        val doubleValue = i * 0.1
        paintingContext.gc.strokeRect(0.0, 0.0, doubleValue, doubleValue)
      }
    }
  }
}
