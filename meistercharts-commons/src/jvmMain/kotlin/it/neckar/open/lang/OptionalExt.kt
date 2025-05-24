package it.neckar.open.lang

import java.util.Optional

/**
 * Converts this value to an [Optional].
 * `null` represents an empty value.
 */
fun <T> T?.toOptional(): Optional<T & Any> {
  return Optional.ofNullable(this)
}
