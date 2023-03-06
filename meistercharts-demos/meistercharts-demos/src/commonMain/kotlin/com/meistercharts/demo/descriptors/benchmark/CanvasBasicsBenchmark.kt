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
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.saved

/**
 * Benchmarks for basic canvas operations
 */
object CanvasBasicsBenchmark {
  val benchmarkOperations: List<BenchmarkOperation> = listOf(
    BenchmarkOperation("saved", 10_000, this::saved),
    BenchmarkOperation("resetTransform", 10_000, this::resetTransform),
    BenchmarkOperation("translate", 100_000, this::translate),
    BenchmarkOperation("rotate", 100_000, this::rotate),
    BenchmarkOperation("scale", 100_000, this::scale),
    BenchmarkOperation("clip", 100_000, this::clip),
    BenchmarkOperation("clearRect", 100_000, this::clearRect)
  )

  private fun resetTransform(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      for (i in 0 until executionCount) {
        paintingContext.gc.resetTransform()
      }
    }
  }

  private fun saved(paintingContext: LayerPaintingContext, executionCount: Int) {
    for (i in 0 until executionCount) {
      callSavedRecursive(paintingContext.gc, 6)
    }
  }

  private fun callSavedRecursive(gc: CanvasRenderingContext, executionCount: Int) {
    gc.saved {
      val newCount = executionCount - 1
      if (newCount == 0) {
        return@saved
      }

      callSavedRecursive(gc, newCount)
    }
  }

  private fun clearRect(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      for (i in 0 until executionCount) {
        paintingContext.gc.clearRect(0.0, 0.0, 10.0 + i * 0.01, 10.0 + i * 0.01)
      }
    }
  }


  private fun clip(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      for (i in 0 until executionCount) {
        val doubleValue = i * 0.1
        paintingContext.gc.clip(doubleValue, doubleValue, doubleValue, doubleValue)
      }
    }
  }

  private fun scale(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      for (i in 0 until executionCount) {
        val doubleValue = i * 0.1
        paintingContext.gc.scale(doubleValue, doubleValue)
      }
    }
  }

  private fun rotate(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      for (i in 0 until executionCount) {
        paintingContext.gc.rotateDegrees(i * 0.1)
      }
    }
  }

  private fun translate(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      for (i in 0 until executionCount) {
        val doubleValue = i * 00.1
        paintingContext.gc.translate(doubleValue, doubleValue)
      }
    }
  }
}
