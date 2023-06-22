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
package com.meistercharts.history.impl.io

import com.meistercharts.history.impl.HistoryValues
import it.neckar.open.collections.DoubleArray2
import it.neckar.open.collections.IntArray2
import it.neckar.open.kotlin.serializers.DoubleArray2Serializer
import it.neckar.open.kotlin.serializers.IntArray2Serializer
import kotlinx.serialization.Serializable

/**
 * Converts the int array to an int array with relative values
 */
fun IntArray2.makeAbsolute(): IntArray2 {
  if (width == 0 || height == 0) {
    return IntArray2(width, height, 0)
  }

  val absolute = IntArray2(width, height, 0)

  //Iterate over cols first - we want to calculate relative values for each data series
  for (x in 0 until this.width) {
    //copy the first entry
    absolute[x, 0] = this[x, 0]

    for (y in 1 until this.height) {
      val previous = absolute[x, y - 1] //read the previous *absolute* value
      val current = this[x, y]

      val absoluteValue = current + previous
      absolute[x, y] = absoluteValue
    }
  }

  return absolute
}

fun DoubleArray2.makeAbsolute(): DoubleArray2 {
  if (width == 0 || height == 0) {
    return DoubleArray2(width, height, 0.0)
  }

  val absolute = DoubleArray2(width, height, 0.0)

  //Iterate over cols first - we want to calculate relative values for each data series
  for (x in 0 until this.width) {
    //copy the first entry
    absolute[x, 0] = this[x, 0]

    for (y in 1 until this.height) {
      val previous = absolute[x, y - 1] //read the previous *absolute* value
      val current = this[x, y]

      val absoluteValue = current + previous
      absolute[x, y] = absoluteValue
    }
  }

  return absolute
}

/**
 * Returns a copy with relative values (relative to the previous value)
 */
fun IntArray2.makeRelative(): IntArray2 {
  if (width == 0 || height == 0) {
    return IntArray2(width, height, 0)
  }

  val relative = IntArray2(width, height, 0)

  //Iterate over cols first - we want to calculate relative values for each data series
  for (x in 0 until this.width) {
    //copy the first entry
    relative[x, 0] = this[x, 0]

    for (y in 1 until this.height) {
      val previous = this[x, y - 1]
      val current = this[x, y]

      val delta = current - previous
      relative[x, y] = delta
    }
  }

  return relative
}

fun DoubleArray2.makeRelative(): DoubleArray2 {
  if (width == 0 || height == 0) {
    return DoubleArray2(width, height, 0.0)
  }

  val relative = DoubleArray2(width, height, 0.0)

  //Iterate over cols first - we want to calculate relative values for each data series
  for (x in 0 until this.width) {
    //copy the first entry
    relative[x, 0] = this[x, 0]

    for (y in 1 until this.height) {
      val previous = this[x, y - 1]
      val current = this[x, y]

      val delta = current - previous
      relative[x, y] = delta
    }
  }

  return relative
}
