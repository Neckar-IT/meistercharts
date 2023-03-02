package com.meistercharts.canvas.layout.cache

import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachFiltered
import it.neckar.open.collections.fastForEachReverse
import it.neckar.open.collections.fastForEachWithIndex

/**
 * Abstract base class for objects cache.
 * This class accepts all kind of objects (e.g. Strings).
 *
 * It is simpler to use [LayoutVariableObjectCache] if the object extend [LayoutVariable]
 */
abstract class AbstractLayoutVariableObjectCache<T>(
  /**
   * The factory that is used to create new elements.
   *
   * ATTENTION: The factory is only called once for each index.
   * The created objects are reused for each layout pass afterwards.
   */
  val factory: () -> T
) : LayoutVariablesCache, LayoutVariableWithSize {

  /**
   * Contains the layout objects, that can be used for layout.
   * This list must never be used directly. Instead use [values]
   */
  internal val objectsStock: MutableList<T> = mutableListOf()

  /**
   * Holds the layout variables.
   * This list is always resized / refilled on size changes.
   *
   * This list has the correct size
   */
  @PublishedApi
  internal val values: MutableList<T> = mutableListOf()

  override val size: Int
    get() {
      return values.size
    }

  fun isEmpty(): Boolean {
    return values.isEmpty()
  }

  /**
   * Ensures the array has the given size
   */
  override fun ensureSize(size: Int) {
    //Increase the size of the objects stock if necessary
    while (objectsStock.size < size) {
      objectsStock.add(factory())
    }

    //If the size is correct, return immediately
    if (values.size == size) {
      return
    }

    //Shrink if necessary
    if (values.size > size) {
      while (values.size > size) {
        values.removeLast()
      }
      return
    }

    //We must increase the size

    //To avoid problems with sorting, recreate the list
    values.clear()

    //Increase if necessary
    while (values.size < size) {
      values.add(objectsStock[values.size])
    }
  }

  /**
   * Returns the element at the given index.
   */
  operator fun get(index: Int): T {
    return values[index]
  }

  inline fun fastForEach(callback: (value: T) -> Unit) {
    values.fastForEach(callback)
  }

  inline fun fastForEachFiltered(predicate: (T) -> Boolean, callback: (value: T) -> Unit) {
    values.fastForEachFiltered(predicate, callback)
  }

  inline fun fastForEachReverse(callback: (value: T) -> Unit) {
    values.fastForEachReverse(callback)
  }

  inline fun fastForEachWithIndex(callback: (index: Int, value: T) -> Unit) {
    values.fastForEachWithIndex(callback)
  }

  /**
   * Removes all elements the given predicate returns true for.
   */
  inline fun removeAll(noinline predicate: (T) -> Boolean) {
    values.removeAll(predicate)
  }

  /**
   * Sorts the list using the given comparator
   */
  fun sortWith(comparator: Comparator<T>) {
    values.sortWith(comparator)
  }

  override fun toString(): String {
    return values.toString()
  }
}

