package it.neckar.open.context

/**
 * Represents the current context.
 * The value can be updated using ThreadLocal (JVM) or a stack (JS)
 */
actual class Context<T> actual constructor(
  initial: T,
) {
  /**
   * Holds the current value.
   * Do *not* use this directly. Use the [with] method instead.
   */
  @Deprecated("Use the with method instead")
  val threadLocalValue: ThreadLocal<T?> = ThreadLocal.withInitial { null } //set null to be able to update the default value later on

  /**
   * The default value that is returned, if no [with] block is active.
   */
  private var defaultValue: T = initial

  /**
   * Sets the default value.
   * Handle with care! In most cases it is better to use [with].
   */
  actual fun setDefaultValue(value: T) {
    this.defaultValue = value
  }

  /**
   * The current value.
   */
  actual val current: T
    @Suppress("DEPRECATION")
    get() = threadLocalValue.get() ?: defaultValue

  /**
   * Executes this block with the updated value
   */
  actual inline fun with(updated: T, block: () -> Unit) {
    @Suppress("DEPRECATION")
    val original = threadLocalValue.get()

    try {
      //Set the new value
      @Suppress("DEPRECATION")
      threadLocalValue.set(updated)

      //Run the block with the updated value
      block()
    } finally {
      //Revert the value
      @Suppress("DEPRECATION")
      threadLocalValue.set(original)
    }
  }
}
