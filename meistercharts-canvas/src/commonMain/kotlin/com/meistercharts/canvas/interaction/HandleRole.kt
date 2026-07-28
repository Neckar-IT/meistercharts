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
package com.meistercharts.canvas.interaction

/**
 * The role an interactive region plays for the element it belongs to.
 *
 * The role decides which region wins when several regions contain the same location:
 * a region with the higher [hitPriority] wins over a region with a lower one.
 * Regions that share a [hitPriority] are decided by registration order - the region registered last wins.
 */
enum class HandleRole(
  /**
   * Regions with a higher priority are hit before regions with a lower priority.
   */
  val hitPriority: Int,
) {
  /**
   * The area of the element itself.
   */
  Body(0),

  /**
   * A handle that resizes the element.
   */
  ResizeHandle(10),

  /**
   * A button that adds the element back after it has been removed.
   */
  AddButton(20),

  /**
   * A button that rotates the element.
   */
  RotateButton(20),

  /**
   * A button that deletes the element.
   */
  DeleteButton(20),
  ;

  companion object {
    /**
     * All priorities that any role uses, highest first.
     * A hit test walks these in order and returns the first match.
     */
    val descendingHitPriorities: List<Int> = entries.map { it.hitPriority }.distinct().sortedDescending()
  }
}
