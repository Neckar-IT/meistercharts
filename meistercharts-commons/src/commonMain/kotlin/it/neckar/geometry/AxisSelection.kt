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
package it.neckar.geometry

/**
 * This enumeration represents a selection of axis.
 * Either both or just the x or y axis can be selected.
 */
enum class AxisSelection(
  /**
   * If the selection contains the x axis, this property returns true
   */
  val containsX: Boolean,
  /**
   * If the selection contains the y axis, this property returns true
   */
  val containsY: Boolean
) {
  /**
   * Zoom over both axis
   */
  Both(true, true),

  /**
   * Only zoom the x axis
   *
   * @noinspection FieldNamingConvention
   */
  X(true, false),

  /**
   * Only zoom the y axis
   *
   * @noinspection FieldNamingConvention
   */
  Y(false, true),

  /**
   * Do not zoom any axis
   */
  None(false, false);

  /**
   * Returns true if the given axis is contained in the selection
   */
  fun contains(axis: Axis): Boolean {
    return when (axis) {
      Axis.X -> containsX
      Axis.Y -> containsY
    }
  }

  /**
   * Returns the negated selection
   */
  fun negate(): AxisSelection {
    return when (this) {
      Both -> None
      X    -> Y
      Y    -> X
      None -> Both
    }
  }

  companion object {
    /**
     * Returns the axis selection
     */
    fun get(xSelected: Boolean, ySelected: Boolean): AxisSelection {
      return values().firstOrNull {
        it.containsX == xSelected && it.containsY == ySelected
      } ?: throw IllegalStateException("No Axis selection found for <$xSelected>, <$ySelected>")
    }
  }
}
