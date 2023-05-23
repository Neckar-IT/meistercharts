package it.neckar.open.collections

/**
 * Avoids unnecessary boxing
 */
fun interface DoublePredicate {
  operator fun invoke(value: Double): Boolean
}
