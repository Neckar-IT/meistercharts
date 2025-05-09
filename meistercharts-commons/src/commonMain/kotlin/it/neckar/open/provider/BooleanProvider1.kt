package it.neckar.open.provider

import it.neckar.open.annotations.NotBoxed

/**
 * Provides a *single* boolean - to avoid boxing
 */
fun interface BooleanProvider1<P1> {
  /**
   * Provides the boolean value
   */
  operator fun invoke(param1: P1): @NotBoxed Boolean

  companion object {
    private val True: BooleanProvider1<Any?> = BooleanProvider1 { true }
    private val False: BooleanProvider1<Any?> = BooleanProvider1 { false }

    fun <P1> True(): BooleanProvider1<P1> {
      return True as BooleanProvider1<P1>
    }

    fun <P1> False(): BooleanProvider1<P1> {
      return False as BooleanProvider1<P1>
    }
  }
}
