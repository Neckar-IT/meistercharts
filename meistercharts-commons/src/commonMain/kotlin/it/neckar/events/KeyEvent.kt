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

import it.neckar.open.unit.time.RelativeMillis

/**
 * Sealed base class for platform-independent key events
 */
sealed class KeyEvent(
  relativeTimestamp: @RelativeMillis Double,

  /**
   * Describing the key code: e.g. "HOME", "F1" or "A"
   */
  val text: String,

  /**
   * The keystroke
   */
  val keyStroke: KeyStroke,
) : UiEvent(relativeTimestamp)

/**
 * A platform-independent key-typed event
 */
class KeyTypeEvent(
  relativeTimestamp: @RelativeMillis Double,
  /**
   * Describing the key code: e.g. "HOME", "F1" or "A"
   */
  text: String,

  /**
   * The key stroke
   */
  keyStroke: KeyStroke
) : KeyEvent(relativeTimestamp, text, keyStroke) {
  override fun toString(): String {
    return "Key Type(text='$text', keyStroke=${keyStroke.description()})"
  }
}

/**
 * A platform-independent key-pressed event
 */
class KeyDownEvent(
  relativeTimestamp: @RelativeMillis Double,
  /**
   * Describing the key code: e.g. "HOME", "F1" or "A"
   */
  text: String,

  /**
   * The key stroke
   */
  keyStroke: KeyStroke
) : KeyEvent(relativeTimestamp, text, keyStroke) {
  override fun toString(): String {
    return "Key Down(text='$text', keyStroke=${keyStroke.description()})"
  }
}

/**
 * A platform-independent key-released event
 */
class KeyUpEvent(
  relativeTimestamp: @RelativeMillis Double,
  /**
   * Describing the key code: e.g. "HOME", "F1" or "A"
   */
  text: String,

  /**
   * The keystroke
   */
  keyStroke: KeyStroke,
) : KeyEvent(relativeTimestamp, text, keyStroke) {
  override fun toString(): String {
    return "Key Up(text='$text', keyStroke=${keyStroke.description()})"
  }
}

/**
 * Returns whether the given event matches the key combination
 */
fun KeyStroke.matches(event: KeyEvent): Boolean {
  return event.keyStroke == this
}
