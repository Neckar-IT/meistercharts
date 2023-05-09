package it.neckar.open.collections

/**
 * A queue which automatically evicts elements from the head of the queue when
 * attempting to add new elements onto the queue and it is full
 */
class EvictingQueue<E>(
  val maxSize: Int
) : MutableCollection<E> {
  private val delegate: ArrayDeque<E> = ArrayDeque()

  init {
    require(maxSize > 0) { "Max size must be > 0 but was <$maxSize>" }
  }

  /**
   * Returns the remaining capacity for new elements before the queue starts evicting.
   */
  fun remainingCapacity(): Int {
    return maxSize - size
  }

  override val size: Int
    get() = delegate.size

  override
  fun contains(element: E): Boolean {
    return delegate.contains(element)
  }

  override fun containsAll(elements: Collection<E>): Boolean {
    return delegate.containsAll(elements)
  }

  override fun isEmpty(): Boolean {
    return delegate.isEmpty()
  }

  override fun iterator(): MutableIterator<E> {
    return delegate.iterator()
  }

  override fun add(element: E): Boolean {
    while (size >= maxSize) {
      delegate.removeFirst()
    }

    return delegate.add(element)
  }

  override fun addAll(elements: Collection<E>): Boolean {
    if (elements.isEmpty()) {
      return false
    }

    //Trying to add same or more elements than the size
    if (elements.size >= maxSize) {
      clear()

      return this.delegate.addAll(
        elements
          .drop(elements.size - maxSize)
      )
    }

    //Default case - not too many elements
    while (size > (maxSize - elements.size)) {
      delegate.removeFirst()
    }
    return delegate.addAll(elements)
  }

  override fun clear() {
    return delegate.clear()
  }

  override fun remove(element: E): Boolean {
    return delegate.remove(element)
  }

  override fun removeAll(elements: Collection<E>): Boolean {
    return delegate.removeAll(elements)
  }

  override fun retainAll(elements: Collection<E>): Boolean {
    return delegate.retainAll(elements)
  }
}
