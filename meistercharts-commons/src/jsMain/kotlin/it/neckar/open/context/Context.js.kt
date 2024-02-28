package it.neckar.open.context

/**
 * Represents the current context.
 * The value can be updated using ThreadLocal (JVM) or a stack (JS)
 */
actual class Context<T> actual constructor(
  initial: T,
) {
  /**
   * The values stack - used to store the current value for a with block.
   * Do *not* use this directly. Use the [with] method instead.
   */
  @Deprecated("Use the with method instead")
  val valuesStack: MutableList<T> = mutableListOf()

  /**
   * The default value that is returned, if no [with] block is active.
   */
  actual var defaultValue: T = initial

  /**
   * The current value.
   */
  actual val current: T
    get() {
      @Suppress("DEPRECATION")
      return valuesStack.lastOrNull() ?: defaultValue
    }

  /**
   * Executes this block with the updated value
   */
  actual inline fun with(updated: T, block: () -> Unit) {
    try {
      //Set the new value
      @Suppress("DEPRECATION")
      valuesStack.add(updated)

      //Run the block with the updated value
      block()
    } finally {
      //Revert the value
      @Suppress("DEPRECATION")
      valuesStack.removeAt(valuesStack.size - 1)
    }
  }
}
