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

import it.neckar.open.annotations.Hot
import it.neckar.open.annotations.HotBoundary

/**
 * A layout variable that has a size.
 *
 * ATTENTION: Usually this does not shrink - only grow.
 */
interface LayoutVariableWithSize : LayoutVariable {
  /**
   * Combined entry point: [resize]s to the given size and then [reset]s all values to their defaults.
   *
   * This is the safe default: after [prepare] the buffer has the requested size and all values are defined.
   */
  @Hot
  fun prepare(size: Int) {
    @HotBoundary("LayoutVariableWithSize dispatch - the interface stays un-colored to avoid forcing @Hot onto every buffer implementation; the hot-path buffers color their resize individually")
    resize(size)
    //It is important to reset *after* the resize
    //because the reset implementation might use the size
    @HotBoundary("LayoutVariableWithSize dispatch - the interface stays un-colored to avoid forcing @Hot onto every buffer implementation; the hot-path buffers color their reset individually")
    reset()
  }

  /**
   * Resets all values to their defaults *without* changing the size.
   *
   * Use [prepare] if the size should be adjusted as well.
   */
  override fun reset()

  /**
   * Grows this variable to (at least) the given size, leaving the values undefined.
   *
   * The caller is expected to overwrite every slot afterward.
   * This is grow-only: usually the size is not shrunk.
   *
   * Implementations decide how to handle the growth:
   * - creation of only additional objects
   * - recreation of all objects
   * - ....
   *
   * Use [prepare] if the values should also be reset to their defaults.
   */
  fun resize(size: Int)

  /**
   * Throws an exception if the index is invalid
   */
  @Hot
  fun verifyIndex(index: Int) {
    if (index < 0 || index >= size) {
      throw IndexOutOfBoundsException("Index $index is out of bounds [0, $size)")
    }
  }

  /**
   * Returns the current size of the buffer - as has been set by [resize] before.
   * Initially, this value is (usually) 0
   */
  val size: Int

  fun isEmpty(): Boolean {
    return size == 0
  }
}
