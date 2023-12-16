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
