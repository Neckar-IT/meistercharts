package it.neckar.open.provider

/**
 * Provides a *single* double - to avoid boxing
 */
fun interface DoubleProvider1<P1> {
  /**
   * Provides the double
   */
  operator fun invoke(param1: P1): Double
}
