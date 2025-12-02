package it.neckar.open.kotlin.lang


/**
 * Helper method that allows to mark a block as deprecated
 */
@Deprecated("Do not use this anymore", level = DeprecationLevel.WARNING)
fun deprecated(message: String, block: () -> Unit) {
  block()
}
