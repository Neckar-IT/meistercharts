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
package com.meistercharts.canvas.layout.cache

import com.meistercharts.canvas.PaintingLoopIndex

/**
 * Contains a layout variable for each key.
 */
class MappedLayoutCache<Key, LayoutVariableType : LayoutVariable>(
  /**
   * Creates a new element - if necessary
   */
  val factory: () -> LayoutVariableType,
) {

  /**
   * Contains the layout objects, that can be used for layout.
   * This list must never be used directly. Use [values] instead.
   *
   * Call [LayoutVariable.reset] for each object from the stock.
   */
  internal val objectsStock: MutableMap<Key, LayoutVariableType> = mutableMapOf()

  /**
   * Holds the layout variables.
   * This map has the correct size
   */
  @PublishedApi
  internal val values: MutableMap<Key, LayoutVariableType> = mutableMapOf()

  /**
   * Returns the object for the given key
   */
  fun get(key: Key): LayoutVariableType {
    return values.getOrPut(key) {
      objectsStock.getOrPut(key) {
        factory()
      }.also { it.reset() }
    }
  }

  fun clear() {
    values.clear()
  }

  /**
   * Contains the current loop index.
   * Is required to be able to detect when a new loop has started and reset everything
   */
  private var currentLoopIndex = PaintingLoopIndex.Unknown

  /**
   * Resets everything if the loop index has changed.
   * Remembers the new loop index.
   */
  fun resetIfNewLoopIndex(paintingLoopIndex: PaintingLoopIndex) {
    if (currentLoopIndex != paintingLoopIndex) {
      values.clear()
      currentLoopIndex = paintingLoopIndex
    }
  }
}
