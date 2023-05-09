package it.neckar.open.provider

import kotlin.reflect.KProperty0

/**
 * Provides a *single* int - to avoid boxing
 */
fun interface IntProvider {
  /**
   * Provides the int
   */
  operator fun invoke(): Int

  companion object {
    /**
     * Always returns [Int.MAX_VALUE]
     */
    val Max: IntProvider = IntProvider { Int.MAX_VALUE }

    /**
     * Always returns [0.0]
     */
    val Zero: IntProvider = IntProvider { 0 }
  }
}

/**
 * Wraps the given int in a IntProvider
 */
fun Int.asIntProvider(): IntProvider {
  return IntProvider { this }
}

/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun KProperty0<IntProvider>.delegate(): IntProvider {
  return IntProvider {
    get().invoke()
  }
}
