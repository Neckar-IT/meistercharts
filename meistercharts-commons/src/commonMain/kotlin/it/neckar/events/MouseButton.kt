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
  Forward;

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
}
