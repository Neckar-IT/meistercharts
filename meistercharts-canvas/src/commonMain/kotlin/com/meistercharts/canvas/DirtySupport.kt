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

/**
 * Supports the dirty state
 */
class DirtySupport {
  /**
   * Whether we are dirty or not.
   */
  var dirty: Boolean = false
    private set

  /**
   * Sets the dirty-state to `true`
   * @see [clearIsDirty]
   */
  fun markAsDirty() {
    dirty = true
  }

  /**
   * Sets the dirty-state to `false`
   * @see [markAsDirty]
   */
  fun clearIsDirty() {
    dirty = false
  }

  /**
   * Calls the given function if dirty state is `true`
   */
  inline fun ifDirty(function: () -> Unit) {
    if (!dirty) {
      return
    }
    clearIsDirty()

    function()
  }
}
