package it.neckar.open.collections

/**
 * A circular buffer with a fixed capacity.
 * The buffer is filled from the head and emptied from the tail.
 * If the buffer is full, the oldest element is removed when a new element is added.
 */
class CircularBuffer<T>(private val capacity: Int) {
  private val buffer = arrayOfNulls<Any?>(capacity)
  private var head = 0
  private var tail = 0
  private var size = 0

  fun add(element: T) {
    buffer[head] = element
    head = (head + 1) % capacity
    if (size < capacity) {
      size++
    } else {
      tail = (tail + 1) % capacity // If the buffer is full, move the tail pointer
    }
  }

  fun get(index: Int): T {
    if (index < 0 || index >= size) {
      throw IndexOutOfBoundsException("Index: $index, Size: $size")
    }
    return buffer[(tail + index) % capacity] as T
  }

  fun size(): Int {
    return size
  }

  /**
   * Converts the entries in the buffer to a list.
   */
  fun toList(): List<T> {
    return (0 until size)
      .map {
        get(it)
      }
  }
}
