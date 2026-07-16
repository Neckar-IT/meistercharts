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
package com.meistercharts.canvas.layout.buffer

import com.meistercharts.annotations.Domain
import it.neckar.open.annotations.Hot
import it.neckar.open.unit.number.MayBeNaN

/**
 * Buffers ticks: the domain value and its formatted label - without creating any objects.
 * Combines a [DoubleMultiBuffer] (tick values) and a [StringMultiBuffer] (formatted labels), indexed by the same tick index.
 *
 * Is *NOT* thread safe!
 */
class TickLabelsBuffer : LayoutVariableWithSize {
  /**
   * The tick domain values.
   * Public for read access (e.g. [DoubleMultiBuffer.fastAny], [DoubleMultiBuffer.lastOr]) - write through [set].
   */
  val values: @Domain @MayBeNaN DoubleMultiBuffer = DoubleMultiBuffer()

  /**
   * The formatted tick labels
   */
  @PublishedApi
  internal val formattedLabels: StringMultiBuffer = StringMultiBuffer()

  override val size: Int
    get() = values.size

  @Hot
  override fun reset() {
    values.reset()
    formattedLabels.reset()
  }

  @Hot
  override fun resize(size: Int) {
    values.resize(size)
    formattedLabels.resize(size)
  }

  /**
   * Sets the tick value and its formatted label for the given index
   */
  @Hot
  fun set(index: Int, value: @Domain @MayBeNaN Double, formatted: String) {
    values[index] = value
    formattedLabels[index] = formatted
  }

  /**
   * Returns the tick value for the given index
   */
  @Hot
  fun valueAt(index: Int): @Domain @MayBeNaN Double {
    return values[index]
  }

  /**
   * Returns the formatted label for the given index
   */
  @Hot
  fun formattedAt(index: Int): String {
    return formattedLabels[index]
  }

  /**
   * Iterates over all ticks
   */
  inline fun fastForEachIndexed(action: (index: Int, value: @Domain @MayBeNaN Double, formatted: String) -> Unit) {
    values.fastForEachIndexed { index, value ->
      action(index, value, formattedLabels[index])
    }
  }

  /**
   * Iterates over all tick values (without the labels)
   */
  inline fun fastForEachValue(action: (value: @Domain @MayBeNaN Double) -> Unit) {
    values.fastForEach(action)
  }
}
