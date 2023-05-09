package it.neckar.open.collections

/**
 * Simple implementation of a linked list
 */
class LinkedList<T> {
  /**
   * The first element
   */
  var first: Element<T>? = null
  /**
   * The last element
   */
  var last: Element<T>? = null

  /**
   * The value of the first element
   */
  val firstValue: T? get() = first?.value
  /**
   * The value of the last element
   */
  val lastValue: T? get() = last?.value

  /**
   * Calculates the list length
   */
  fun getLength(): Int {
    var length = 0
    if (first == null) {
      return length
    }

    length++

    var check = first
    while (check?.next != null) {
      length++
      check = check.next
    }

    return length
  }

  /**
   * Add an element at first position
   */
  fun addFirst(value: T) {
    when (first) {
      null -> addWhenEmpty(value)
      else -> {
        val currentFirst = first
        first = Element(value, null, currentFirst)
        currentFirst?.previous = first
      }
    }
  }

  /**
   * Adds a new value at the end
   */
  fun addLast(value: T) {
    when (last) {
      null -> addWhenEmpty(value)
      else -> {
        val currentLast = last
        last = Element(value, currentLast, null)
        currentLast?.next = last
      }
    }
  }

  private fun addWhenEmpty(value: T) {
    val newElement = Element(value, null, null)
    first = newElement
    last = newElement
  }


  /**
   * Removes and returns the last value
   */
  fun pollLast(): T? {
    val currentLast = last
    last = last?.previous
    last?.next = null

    if (last == null) {
      first = null
    }
    return currentLast?.value
  }

  /**
   * Removes and returns the first value
   */
  fun pollFirst(): T? {
    val currentFirst = first
    first = first?.next
    first?.previous = null

    if (first == null) {
      last = null
    }
    return currentFirst?.value
  }

  /**
   * Clears the list
   */
  fun clear() {
    first = null
    last = null
  }
}

/**
 * Represents one element within the linked list
 */
data class Element<T>(
  val value: T,
  var previous: Element<T>?,
  var next: Element<T>?


) {
  override fun toString(): String {
    return "element: $value, hasPrevious: ${previous != null}, hasNext: ${next != null}"
  }
}

