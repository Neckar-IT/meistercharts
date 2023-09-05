package it.neckar.open.provider

import it.neckar.open.annotations.NotBoxed

/**
 * Provides a *single* double - to avoid boxing
 */
fun interface DoubleProvider1<P1> {
  /**
   * Provides the double
   */
  operator fun invoke(param1: P1): @NotBoxed Double
}
