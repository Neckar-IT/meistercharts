package it.neckar.open.kotlin.lang

/**
 * Supports simple reflection - must not be used for production.
 * Only for debugging
 */


/**
 * Returns kind of a properties' description.
 * ATTENTION: The names will be garbage (in Kotlin/JS)
 */
expect fun props(value: Any): Map<String, Any?>
