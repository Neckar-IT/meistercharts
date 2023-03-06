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
import it.neckar.open.collections.DoubleArray2
import it.neckar.open.collections.DoubleArrayList

/**
 * Benchmarks related to data series
 */
object DataSeriesBenchmarks {
  val benchmarkOperations: List<BenchmarkOperation> = listOf(
    BenchmarkOperation("arrayCopy", 1000, this::arrayCopy),
    BenchmarkOperation("initializeArrayWithData", 1000, this::initializeArrayWithData),
    BenchmarkOperation("arrayCopyWithData", 1000, this::arrayCopyWithData),
    BenchmarkOperation("Create List<Double>", 100, this::listOfDoublesCreation),
    BenchmarkOperation("doubleArrayList", 1000, this::doubleArrayList),
    BenchmarkOperation("doubleArrayList With Data", 1000, this::doubleArrayListWithData),
    BenchmarkOperation("doubleArrayListCopy", 1000, this::doubleArrayListCopy),
    BenchmarkOperation("array2Instantiate", 1000, this::array2Instantiate),
    BenchmarkOperation("array2Dimensional", 1000, this::array2Dimensional)
  )

  private fun arrayCopy(paintingContext: LayerPaintingContext, executionCount: Int) {
    var array = DoubleArray(100_000)

    for (i in 0 until executionCount) {
      array = array.copyOf(array.size)
    }
  }

  private fun initializeArrayWithData(paintingContext: LayerPaintingContext, executionCount: Int) {
    for (i in 0 until executionCount) {
      var array = DoubleArray(100_000) {
        it.toDouble()
      }
    }
  }

  private fun arrayCopyWithData(paintingContext: LayerPaintingContext, executionCount: Int) {
    var array = DoubleArray(100_000) {
      it.toDouble()
    }

    for (i in 0 until executionCount) {
      array = array.copyOf(array.size)
    }
  }

  private fun listOfDoublesCreation(paintingContext: LayerPaintingContext, executionCount: Int) {
    for (i in 0 until executionCount) {
      var array = List<Double>(100_000) { 0.0 }
    }
  }

  private fun doubleArrayList(paintingContext: LayerPaintingContext, executionCount: Int) {
    for (i in 0 until executionCount) {
      val doubleArrayList = DoubleArrayList(100_000)
    }
  }

  private fun doubleArrayListWithData(paintingContext: LayerPaintingContext, executionCount: Int) {
    for (i in 0 until executionCount) {
      val doubleArrayList = DoubleArrayList(100_000)

      for (i in 0..100_000) {
        doubleArrayList.add(i.toDouble())
      }
    }
  }

  private fun doubleArrayListCopy(paintingContext: LayerPaintingContext, executionCount: Int) {
    val doubleArrayList = DoubleArrayList(100_000)
    for (i in 0..100_000) {
      doubleArrayList.add(i.toDouble())
    }

    for (i in 0 until executionCount) {
      val newList = DoubleArrayList(doubleArrayList)
    }
  }

  private fun array2Instantiate(paintingContext: LayerPaintingContext, executionCount: Int) {
    val width = 1_000
    val height = 100
    for (i in 0 until executionCount) {
      val newList = DoubleArray2(width, height) { it.toDouble() }

      //Now access one element
      val d = newList[500, 50]
    }
  }

  private fun array2Dimensional(paintingContext: LayerPaintingContext, executionCount: Int) {
    val width = 1_000
    val height = 100

    for (i in 0 until executionCount) {
      val array = Array(width) {
        DoubleArray(height) { it.toDouble() }
      }

      //Now access one element
      val d = array[500][50]
    }
  }
}
