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
package com.meistercharts.model

/**
 * Describes a corner
 */
enum class Corner {
  TopRight,
  BottomRight,
  BottomLeft,
  TopLeft;

  companion object {
    /**
     * Returns the corner for a given index.
     * This method is (a little bit) faster then `values[index]`
     */
    fun get(index: Int): Corner {
      return when (index) {
        0    -> TopRight
        1    -> BottomRight
        2    -> BottomLeft
        3    -> TopLeft
        else -> throw IllegalArgumentException("Invalid index <$index>")
      }
    }
  }
}
