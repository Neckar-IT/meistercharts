package it.neckar.open.kotlin.lang

import js.core.Object

/**
 * Returns kind of a properties' description.
 * ATTENTION: The names will be garbage (in Kotlin/JS)
 */
actual fun props(value: Any): Map<String, Any?> {
  return Object.entries(value).associate {
    Pair(it.component1(), it.component2())
  }
}
