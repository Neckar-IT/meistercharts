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
 * Represents a key code.
 *
 * Take a look at KeyCodes (in the tests source) for predefined values
 *
 * Attention: This class contains the key codes as defined in HTML.
 * Other platforms (e.g. JavaFX) have sometimes different key codes. The mapping is done in the platform specific code.
 */
data class KeyCode(
  /**
   * The key code
   */
  val code: Int,
) {
  constructor(char: Char) : this(char.code)

  companion object {
    /**
     * Constant for the non-numpad **left** arrow key.
     */
    val Left: KeyCode = KeyCode(0x25)

    /**
     * Constant for the non-numpad **up** arrow key.
     */
    val Up: KeyCode = KeyCode(0x26)

    /**
     * Constant for the non-numpad **right** arrow key.
     */
    val Right: KeyCode = KeyCode(0x27)

    /**
     * Constant for the non-numpad **down** arrow key.
     */
    val Down: KeyCode = KeyCode(0x28)

    /**
     * Delete key
     */
    val Delete: KeyCode = KeyCode(0x2E)

    /**
     * Escape key
     */
    val Escape: KeyCode = KeyCode(0x1B)

    val Home: KeyCode = KeyCode(0x24)


    val Digit0: KeyCode = KeyCode(48)
    val Digit1: KeyCode = KeyCode(49)

    val Digit0NumPad: KeyCode = KeyCode(96)
    val Digit1NumPad: KeyCode = KeyCode(97)


    val Plus: KeyCode = KeyCode(0x209)
    val PlusNumPad: KeyCode = KeyCode(0x6B)

    val Minus: KeyCode = KeyCode(0x2D)
    val MinusNumPad: KeyCode = KeyCode(0x6D)
  }
}
