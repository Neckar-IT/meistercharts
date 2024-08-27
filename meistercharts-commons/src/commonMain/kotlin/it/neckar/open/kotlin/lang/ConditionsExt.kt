package it.neckar.open.kotlin.lang

/**
 * Repeats the given action as long as the condition is true
 */
inline fun repeatWhile(maxRetries: Int, condition: () -> Boolean, action: () -> Unit) {
  repeat(maxRetries) {
    if (condition().not()) return
    action()
  }
}

/**
 * Repeats the given action as long as the condition is true
 */
inline fun repeatWhile(maxRetries: Int, action: () -> Boolean) {
  require(maxRetries > 0) { "maxRetries must be greater than 0 but was [$maxRetries]" }

  repeat(maxRetries) {
    val result = action()
    if (result.not()) return
  }

  throw IllegalStateException("Condition was never false after $maxRetries retries")
}
