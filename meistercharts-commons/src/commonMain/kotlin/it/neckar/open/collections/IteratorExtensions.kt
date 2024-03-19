package it.neckar.open.collections

/**
 * Contains extension functions for Iterators
 *
 * Copied from https://github.com/LukasForst/katlib/blob/master/ (MIT License)
 */


/**
 * Returns the largest value of the given function or `null` if there are no elements.
 */
inline fun <T, R : Comparable<R>> Iterable<T>.maxValueBy(selector: (T) -> R): R? {
  val iterator = iterator()
  if (!iterator.hasNext()) return null
  var maxValue = selector(iterator.next())
  while (iterator.hasNext()) {
    val v = selector(iterator.next())
    if (maxValue < v) {
      maxValue = v
    }
  }
  return maxValue
}

/**
 * Returns the smallest value of the given function or `null` if there are no elements.
 */
inline fun <T, R : Comparable<R>> Iterable<T>.minValueBy(selector: (T) -> R): R? {
  val iterator = iterator()
  if (!iterator.hasNext()) return null
  var minValue = selector(iterator.next())
  while (iterator.hasNext()) {
    val v = selector(iterator.next())
    if (minValue > v) {
      minValue = v
    }
  }
  return minValue
}


/**
 * Performs the given [action] on each element that is not null.
 */
inline fun <T : Any> Iterable<T?>.forEachNotNull(action: (T) -> Unit) {
  for (element in this) element?.let(action)
}


/**
 * Returns the single element matching the given [predicate], or `null` if element was not found.
 *
 * Throws [IllegalArgumentException] when multiple elements are matching predicate.
 */
inline fun <T> Iterable<T>.singleOrEmpty(predicate: (T) -> Boolean): T? {
  var single: T? = null
  var found = false
  for (element in this) {
    if (predicate(element)) {
      if (found) {
        throw IllegalArgumentException("Collection contains more than one matching element.")
      }
      single = element
      found = true
    }
  }
  return single
}


/**
 * Returns single element, or `null` if the collection is empty.
 * Throws [IllegalArgumentException] when multiple elements are matching predicate.
 */
fun <T> Iterable<T>.singleOrEmpty(): T? =
  when (this) {
    is List ->
      when (size) {
        0 -> null
        1 -> this[0]
        else -> throw IllegalArgumentException("Collection contains more than one element.")
      }

    else -> {
      val iterator = iterator()
      if (!iterator.hasNext()) {
        null
      } else {
        val single = iterator.next()
        if (iterator.hasNext()) {
          throw IllegalArgumentException("Collection contains more than one element.")
        }
        single
      }
    }
  }
