package it.neckar.open.collections

/**
 * Contains extension methods for List
 */
fun <T> List<T>.shifted(shiftCount: Int): List<T> {
  val size = this.size
  if (size == 0 || shiftCount % size == 0) return this

  val effectiveShift = shiftCount % size
  return this.drop(effectiveShift) + this.take(effectiveShift)
}

/**
 * Removes the first element that matches the predicate and returns it or null if no element was found
 */
fun <T> MutableList<T>.removeIfOrNull(predicate: (T) -> Boolean): T? {
  val index = this.indexOfFirst(predicate)
  return if (index != -1) {
    this.removeAt(index)
  } else {
    null
  }
}
