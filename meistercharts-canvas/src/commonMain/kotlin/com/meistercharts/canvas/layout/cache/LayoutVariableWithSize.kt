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

/**
 * A layout variable that has a size.
 *
 * ATTENTION: Usually this does not shrink - only grow.
 */
interface LayoutVariableWithSize : LayoutVariable {
  /**
   * Resets this cache and ensures the given size
   */
  fun prepare(size: Int) {
    ensureSize(size)
    //It is important to reset *after* the resize
    //because the reset implementation might use the size
    reset()
  }

  /**
   * ATTENTION: Please call prepare instead
   */
  override fun reset()

  /**
   * Ensures this variable has (at least) the given size.
   *
   * Implementations decide how to handle resizes:
   * - creation of only additional objects
   * - recreation of all objects
   * - ....
   */
  fun ensureSize(size: Int)

  /**
   * Returns the current size of the cache - as has been set by [ensureSize] before.
   * Initially this value is (usually) 0
   */
  val size: Int
}
