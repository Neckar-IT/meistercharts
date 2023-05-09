package it.neckar.open.kotlin.lang

/**
 * TODO replace with Kotlin build-in function as soon as available
 */
inline fun <reified T : Enum<T>> enumEntries(): List<T> {
  return enumValues<T>().toList()
}
