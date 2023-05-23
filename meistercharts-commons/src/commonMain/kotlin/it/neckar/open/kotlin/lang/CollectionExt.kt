package it.neckar.open.kotlin.lang

import com.meistercharts.annotations.Domain
import it.neckar.open.unit.other.pct

/**
 *
 */


inline fun count(cond: (index: Int) -> Boolean): Int {
  var counter = 0
  while (cond(counter)) counter++
  return counter
}

inline fun <reified T> mapWhile(cond: (index: Int) -> Boolean, gen: (Int) -> T): List<T> = arrayListOf<T>().apply { while (cond(this.size)) this += gen(this.size) }
inline fun <reified T> mapWhileArray(cond: (index: Int) -> Boolean, gen: (Int) -> T): Array<T> = mapWhile(cond, gen).toTypedArray()

fun <T> List<T>.getCyclic(index: Int) = this[index umod this.size]
fun <T> Array<T>.getCyclic(index: Int) = this[index umod this.size]


/**
 * Returns the n th element from the list. Uses modulo if the index is larger than the size of the list
 */
fun <T> List<T>.getModulo(index: Int): T {
  require(isNotEmpty()) { "List must not be empty" }

  //This calculation produces a "wrap around" effect for negative indices
  return this[index.wrapAround(size)]
}

/**
 * Returns the n th element from the list. Uses modulo if the index is larger than the size of the list.
 * Returns null if the list is empty.
 */
fun <T> List<T>.getModuloOrNull(index: Int): T? {
  //This calculation produces a "wrap around" effect for negative indices
  return if (isEmpty()) null else this[index.wrapAround(size)]
}

/**
 * Iterates over this collection delivering its elements to [consumer] while the [consumer] returns true for a consumed element
 * @see [consumeUntil]
 */
inline fun <E> Collection<E>.consumeWhile(consumer: (E) -> Boolean) {
  val iter = iterator()
  while (iter.hasNext()) {
    if (!consumer(iter.next())) {
      return
    }
  }
}

/**
 * Iterates over this collection delivering its elements to [consumer] until the [consumer] returns false for a consumed element
 * @see [consumeWhile]
 * @return true if the consumer returned true for any value. false otherwise
 */
inline fun <E> Collection<E>.consumeUntil(consumer: (E) -> Boolean): Boolean {
  val iter = iterator()
  while (iter.hasNext()) {
    if (consumer(iter.next())) {
      return true
    }
  }

  return false
}

/**
 * Consumes the consumer until it returns the cancel value
 * @return the cancel value or null if no consumer has returned the cancel value
 */
inline fun <E, T> Collection<E>.consumeUntil(cancelValue: T, consumer: (E) -> T): T? {
  val iter = iterator()
  while (iter.hasNext()) {
    if (consumer(iter.next()) == cancelValue) {
      return cancelValue
    }
  }

  return null
}

/**
 * Returns a list of values that have relative values (the sum of all values is equal to 1.0).
 */
fun List<@Domain Double>.toRelativeValues(): List<@pct Double> {
  @Domain val sum = this.sum()
  return map {
    1 / sum * it
  }
}

/**
 * Removes elements from the list until the max size has been reached
 */
fun <T> MutableList<T>.deleteFromStartUntilMaxSize(maxSize: Int) {
  require(maxSize >= 0) { "Invalid max size: $maxSize" }

  while (this.size > maxSize) {
    removeAt(0)
  }
}

/**
 * Sets  the element as last element. Replaces the last element!
 */
fun <E> MutableList<E>.setLast(element: E) {
  this[lastIndex] = element
}

/**
 * Returns a new list containing:
 * * null
 * * all elements from this
 */
fun <E> List<E>.withNullAtFirst(): List<E?> {
  return buildList {
    add(null)
    addAll(this@withNullAtFirst)
  }
}

/**
 * Returns a new list with the given elements at the given position (set)
 */
fun <E> List<E>.withElementAt(index: Int, element: E): List<E> {
  return buildList {
    addAll(this@withElementAt)
    this[index] = element
  }
}
