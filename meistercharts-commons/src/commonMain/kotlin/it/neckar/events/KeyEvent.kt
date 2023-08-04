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
