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
   * The default value that is returned, if no [with] block is active.
   * Usually [with] (to write) and [current] (to read) should be used instead.
   */
  var defaultValue: T

  /**
   * Executes this block with the updated value.
   * Resets the value after the block has been executed.
   */
  inline fun with(updated: T, block: () -> Unit)
}
