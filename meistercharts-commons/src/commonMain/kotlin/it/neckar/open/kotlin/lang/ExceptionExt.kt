package it.neckar.open.kotlin.lang

/**
 * Executes the given [block] and suppresses only exceptions of type [E].
 * All other exceptions are propagated.
 *
 * This is similar to [runCatching] but type-safe - it only catches the specified exception type.
 *
 * Usage:
 * ```
 * suppressException<CancellationException> {
 *   someOperationThatMightCancel()
 * }
 * ```
 *
 * @param block the block to execute
 */
inline fun <reified E : Exception> suppressException(block: () -> Unit) {
  try {
    block()
  } catch (_: E) {
    // Expected exception - suppress it
  }
}
