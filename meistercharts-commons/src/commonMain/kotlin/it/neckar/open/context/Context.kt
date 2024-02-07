package it.neckar.open.context

/**
 * Represents a value within the current context.
 * The value can be updated using ThreadLocal (JVM) or a stack (JS)
 */
expect class Context<T>(initial: T) {
  /**
   * The current value. Might be modified by calls to [with].
   */
  val current: T

  /**
   * Sets the default value.
   * Handle with care! In most cases it is better to use [with].
   */
  fun setDefaultValue(value: T)

  /**
   * Executes this block with the updated value.
   * Resets the value after the block has been executed.
   */
  inline fun with(updated: T, block: () -> Unit)
}
