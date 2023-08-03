package it.neckar.events

import kotlin.jvm.JvmField

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
  val meta: Boolean = false,
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
      meta: Boolean,
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
    val Ctrl: ModifierCombination = ModifierCombination(
      control = true
    )

    /**
     * Helper alias
     */
    @JvmField
    val Control: ModifierCombination = Ctrl

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
