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

import kotlin.jvm.JvmField

/**
 * Represents a key stroke: A key and a combination of modifiers (ctrl, alt ...)
 */
data class KeyStroke(
  /**
   * The key code
   */
  val keyCode: KeyCode,
  /**
   * The modifier combination
   */
  val modifierCombination: ModifierCombination = ModifierCombination.None
) {
  fun description(): String {
    return "[${keyCode.code}] ${modifierCombination.description()}"
  }
}

/**
 * Combinations of modifiers
 */
data class ModifierCombination(
  /**
   * Whether the shift key is pressed
   */
  val shift: Boolean = false,
  /**
   * Whether the control key is pressed
   */
  val control: Boolean = false,
  /**
   * Whether the alt key is pressed
   */
  val alt: Boolean = false,
  /**
   * Whether the meta key is pressed
   */
  val meta: Boolean = false
) {

  fun description(): String {
    val modifiers = mutableListOf<String>()
    if (shift) {
      modifiers.add("SHIFT")
    }
    if (control) {
      modifiers.add("CTRL")
    }
    if (alt) {
      modifiers.add("ALT")
    }
    if (meta) {
      modifiers.add("META")
    }

    return modifiers.joinToString(" ")
  }

  private fun matches(shift: Boolean, control: Boolean, alt: Boolean, meta: Boolean): Boolean {
    return this.shift == shift &&
      this.control == control &&
      this.alt == alt &&
      this.meta == meta
  }


  companion object {
    fun get(
      shift: Boolean,
      control: Boolean,
      alt: Boolean,
      meta: Boolean
    ): ModifierCombination {

      if (None.matches(shift, control, alt, meta)) {
        return None
      }

      if (Alt.matches(shift, control, alt, meta)) {
        return Alt
      }

      if (Control.matches(shift, control, alt, meta)) {
        return Control
      }

      if (Meta.matches(shift, control, alt, meta)) {
        return Meta
      }

      if (CtrlAlt.matches(shift, control, alt, meta)) {
        return CtrlAlt
      }

      if (CtrlShift.matches(shift, control, alt, meta)) {
        return CtrlShift
      }

      if (CtrlShiftAlt.matches(shift, control, alt, meta)) {
        return CtrlShiftAlt
      }

      return ModifierCombination(shift, control, alt, meta)
    }

    /**
     * No modifier is pressed
     */
    @JvmField
    val None: ModifierCombination = ModifierCombination()

    @JvmField
    val Alt: ModifierCombination = ModifierCombination(
      alt = true
    )

    @JvmField
    val Shift: ModifierCombination = ModifierCombination(
      shift = true
    )

    @JvmField
    val Control: ModifierCombination = ModifierCombination(
      control = true
    )

    @JvmField
    val Meta: ModifierCombination = ModifierCombination(
      meta = true
    )

    @JvmField
    val CtrlAlt: ModifierCombination = ModifierCombination(
      control = true,
      alt = true
    )

    @JvmField
    val CtrlShift: ModifierCombination = ModifierCombination(
      shift = true,
      control = true
    )

    @JvmField
    val CtrlShiftAlt: ModifierCombination = ModifierCombination(
      shift = true,
      control = true,
      alt = true
    )
  }
}

/**
 * Returns whether the given event matches the key combination
 */
fun KeyStroke.matches(event: KeyEvent): Boolean {
  return event.keyStroke == this
}
