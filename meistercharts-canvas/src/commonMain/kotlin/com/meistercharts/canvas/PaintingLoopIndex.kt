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
package com.meistercharts.canvas

import kotlin.jvm.JvmInline

/**
 * Identifies the paint loop index.
 *
 * Attention: This variable *will* overflow (from [Int.MAX_VALUE] to `0`.
 * Please do not use the value to do calculations.
 *
 * Assume that the index will overflow after about 414 days (when painting with 60 fps).
 */
@JvmInline
value class PaintingLoopIndex(val value: Int) {
  /**
   * Returns the next painting index.
   * ATTENTION: The value will overflow
   */
  fun next(): PaintingLoopIndex {
    if (value == Int.MAX_VALUE) {
      //Overflow to 0 (not Int.MIN_VALUE)
      return PaintingLoopIndex(0)
    }
    val newIndex = value + 1
    require(newIndex >= 0) { "Invalid new index $newIndex for old value $value" }
    return PaintingLoopIndex(newIndex)
  }

  override fun toString(): String {
    return "$value"
  }

  companion object {
    /**
     * Specifies a loop index that will not happen naturally.
     * Can be used to specify an unknown index.
     */
    val Unknown: PaintingLoopIndex = PaintingLoopIndex(-1)
  }
}
