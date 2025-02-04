package it.neckar.open.lang

import java.util.Optional

/**
 *
 */
fun <T> T?.toOptional(): Optional<T & Any> {
  return Optional.ofNullable(this)
}
