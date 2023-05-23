package it.neckar.open.kotlin.lang

/**
 * Requires that both parameters are equal.
 * Uses the provided lazy message appended with first/second
 * @throws IllegalArgumentException if first and second are not equal
 */
fun <T> requireEquals(first: T, second: T, lazyMessage: () -> Any) {
  require(first == second) {
    "${lazyMessage()}: <$first> != <$second>"
  }
}

/**
 * Checks that both parameters are equal.
 * Uses the provided lazy message appended with first/second
 * @throws IllegalStateException if first and second are not equal
 */
fun <T> checkEquals(first: T, second: T, lazyMessage: () -> Any) {
  check(first == second) {
    "${lazyMessage()}: <$first> != <$second>"
  }
}

/**
 * Suffix notation for checkNotNull
 */
inline fun <T> T?.checkNotNull(lazyMessage: () -> Any): T {
  return checkNotNull(this, lazyMessage)
}
