package com.meistercharts.canvas.layout.cache

import com.meistercharts.model.RightTriangleType
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed

/**
 * Caches double values in an array.
 *
 */
class RightTriangleTypeCache : LayoutVariableWithSize {
  /**
   * Contains the values
   */
  @PublishedApi
  internal var values: Array<RightTriangleType?> = emptyArray()

  override fun reset() {
    values.forEachIndexed { index, _ ->
      values[index] = null
    }
  }

  /**
   * Ensures the array has the given size
   */
  override fun ensureSize(size: Int) {
    if (values.size != size) {
      values = Array(size) { null }
    }
  }

  override val size: Int
    get() = values.size

  operator fun set(index: Int, value: RightTriangleType) {
    values[index] = value
  }

  operator fun get(index: Int): RightTriangleType? {
    return values[index]
  }

  inline fun fastForEachIndexed(callback: (index: Int, value: RightTriangleType?) -> Unit) {
    this.values.fastForEachIndexed(callback)
  }

  inline fun fastForEachIndexedReverse(callback: (index: Int, value: RightTriangleType?, isFirst: Boolean) -> Unit) {
    this.values.fastForEachIndexedReverse(callback)
  }

  inline fun fastForEach(callback: (value: RightTriangleType?) -> Unit) {
    this.values.fastForEach(callback)
  }

  inline fun Array<RightTriangleType?>.fastForEachIndexedReverse(callback: (index: Int, value: RightTriangleType?, isFirst: Boolean) -> Unit) {
    val currentSize = size

    for (i in (currentSize - 1) downTo 0) {
      callback(i, this[i], i == currentSize - 1)
    }
  }
}
