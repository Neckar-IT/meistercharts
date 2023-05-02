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
package com.meistercharts.events

import com.meistercharts.events.UiEvent
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
   * The key stroke
   */
  val keyStroke: KeyStroke
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
   * The key stroke
   */
  keyStroke: KeyStroke
) : KeyEvent(relativeTimestamp, text, keyStroke) {
  override fun toString(): String {
    return "Key Up(text='$text', keyStroke=${keyStroke.description()})"
  }
}

/**
 * Represents a key code.
 *
 * Take a look at KeyCodes (in the tests source) for predefined values
 *
 * Attention: This class contains the key codes as defined in HTML.
 * Other platforms (e.g. JavaFX) have sometimes different key codes. The mapping is done in the platform specific code.
 */
data class KeyCode constructor(
  /**
   * The key code
   */
  val code: Int
) {
  constructor(char: Char) : this(char.toInt())

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
  }
}
