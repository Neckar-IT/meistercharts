/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.events

/**
 * Represents a mouse button
 */
enum class MouseButton {
  /**
   * The "primary" mouse button (usually the left button)
   */
  Primary,

  /**
   * The "secondary" mouse button (usually the right button)
   */
  Secondary,

  /**
   * The "middle" mouse button (usually the one with the mouse wheel)
   */
  Middle,

  /**
   * The "back" mouse button
   */
  Back,

  /**
   * The "forward" mouse button
   */
  Forward,

  /**
   * Represents another mouse button that is not one of the predefined ones
   */
  Other,
  ;
  /**
   * Returns true if this button is the primary button
   */
  fun isPrimary(): Boolean {
    return this == Primary
  }

  /**
   * Returns true if this button is the primary button
   */
  fun isSecondary(): Boolean {
    return this == Secondary
  }

  companion object {
    /**
     * Maps a DOM `MouseEvent.button` code to a [MouseButton]
     * (https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/button):
     * 0 = primary, 1 = auxiliary/middle, 2 = secondary, 3 = fourth button (browser back), 4 = fifth button (browser forward)
     */
    fun fromDomButtonCode(buttonCode: Short): MouseButton {
      return when (buttonCode) {
        0.toShort() -> Primary
        1.toShort() -> Middle
        2.toShort() -> Secondary
        3.toShort() -> Back
        4.toShort() -> Forward
        else -> Other
      }
    }
  }
}
