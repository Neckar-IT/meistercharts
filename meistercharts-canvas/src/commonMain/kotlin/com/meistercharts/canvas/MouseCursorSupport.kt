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

import it.neckar.open.observable.ObservableObject

/**
 * Supports mouse cursor management for the canvas
 */
class MouseCursorSupport(
  private val mouseCursor: ObservableObject<MouseCursor>
) {
  /**
   * Contains requested cursor properties.
   *
   * Different layers can request an cursor in different properties.
   * The first property that contains a non null mouse cursor is used.
   */
  private val requestedCursors: MutableMap<Any, ObservableObject<MouseCursor?>> = mutableMapOf()

  /**
   * Returns the cursor property for the given key (which usually represents a layer)
   */
  fun cursorProperty(key: Any): ObservableObject<MouseCursor?> {
    return requestedCursors.getOrPut(key) {
      val observableObject: ObservableObject<MouseCursor?> = ObservableObject(null)
      observableObject.consumeImmediately {
        updateRequestedCursors()
      }

      observableObject
    }
  }

  /**
   * Clears the property for the given key
   */
  fun clearProperty(key: Any): ObservableObject<MouseCursor?>? {
    return requestedCursors.remove(key)
  }

  private fun updateRequestedCursors() {
    mouseCursor.value = calculateRequestedCursors() ?: MouseCursor.Default
  }

  /**
   * Recalculates the requested cursors
   */
  private fun calculateRequestedCursors(): MouseCursor? {
    return requestedCursors
      .values
      .asSequence()
      .map {
        it.value
      }.filterNotNull()
      .filter { it != MouseCursor.Default } //Skip default mouse cursors
      .firstOrNull()
  }
}

