package com.meistercharts.canvas.layout.cache

import it.neckar.open.collections.emptyIntArray
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed

/**
 * Contains a list of int.
 * This can be used to keep a sorted list of indices
 */
class IntCache : LayoutVariablesCache {
  /**
   * Contains the values
   *
   * Use this only for performance reasons.
   * Altering this instance alters the cache, too.
   */
  @PublishedApi
  internal var values: IntArray = emptyIntArray()
    private set

  /**
   * Resets this cache and ensures the given size
   */
  fun prepare(size: Int) {
    ensureSize(size)
    //It is important to reset *after* the resize
    //because the reset implementation might use the size
    reset()
  }

  override fun reset() {
    values.forEachIndexed { index, _ ->
      values[index] = 0
    }
  }

  /**
   * Ensures the array has the given size
   */
  fun ensureSize(size: Int) {
    if (values.size != size) {
      values = IntArray(size) { 0 }
    }
  }

  val size: Int
    get() = values.size

  operator fun set(index: Int, value: Int) {
    values[index] = value
  }

  /**
   * Returns the n-th index
   */
  operator fun get(index: Int): Int {
    return values[index]
  }

  inline fun fastForEachIndexed(callback: (index: Int, value: Int) -> Unit) {
    this.values.fastForEachIndexed(callback)
  }

  inline fun fastForEach(callback: (value: Int) -> Unit) {
    this.values.fastForEach(callback)
  }
}
