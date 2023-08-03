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

    val Home: KeyCode = KeyCode(36)


    val Digit1: KeyCode = KeyCode('1')
  }
}
