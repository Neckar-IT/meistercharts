package com.meistercharts.canvas.layout.cache

import it.neckar.open.collections.fastForEachWithIndex

/**
 * Caches string values
 */
class StringCache : AbstractLayoutVariableObjectCache<String>({
  Uninitialized
}) {

  override fun reset() {
    values.fastForEachWithIndex { index, _ ->
      values[index] = Uninitialized
    }
  }

  operator fun set(index: Int, value: String) {
    values[index] = value
  }

  companion object {
    const val Uninitialized: String = "UNINITIALIZED"
  }
}
