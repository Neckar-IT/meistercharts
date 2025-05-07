package it.neckar.open.provider

import it.neckar.open.annotations.NotBoxed

/**
 * Provides a *single* boolean - to avoid boxing
 */
fun interface BooleanProvider2<P1, P2> {
  /**
   * Provides the boolean value
   */
  operator fun invoke(param1: P1, param2: P2): @NotBoxed Boolean

  companion object {
    private val True: BooleanProvider = BooleanProvider { true }
    private val False: BooleanProvider = BooleanProvider { false }

    fun <P1, P2> True(): BooleanProvider2<P1, P2> {
      return True as BooleanProvider2<P1, P2>
    }

    fun <P1, P2> False(): BooleanProvider2<P1, P2> {
      return False as BooleanProvider2<P1, P2>
    }
  }
}
