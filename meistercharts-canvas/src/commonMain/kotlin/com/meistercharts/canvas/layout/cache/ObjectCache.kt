package com.meistercharts.canvas.layout.cache

import it.neckar.open.collections.fastForEachWithIndex

/**
 * Caches objects
 */
class ObjectCache<T>(val initialValue: T) : AbstractLayoutVariableObjectCache<T>({
  initialValue
}) {

  override fun reset() {
    values.fastForEachWithIndex { index, _ ->
      values[index] = initialValue
    }
  }

  operator fun set(index: Int, value: T) {
    values[index] = value
  }
}
