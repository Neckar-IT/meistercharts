package com.meistercharts.canvas.layout.cache

import com.meistercharts.algorithms.ZIndex
import com.meistercharts.canvas.layout.cache.ZIndexSortingCache.Entry
import it.neckar.open.collections.genericSort

/**
 * Contains Z-Index Ordering.
 *
 * Do *NOT* use with [fastForEachWithIndex] - since the provided index does not make any sense.
 * Instead, use [fastForEach] and use [Entry.index] instead.
 */
class ZIndexSortingCache : LayoutVariableObjectCache<Entry>(
  { Entry() }
) {
  /**
   * Sort the values by the z-index
   */
  fun sortByZIndex() {
    values.genericSort() //use generic sort to avoid instantiation of arrays
  }

  operator fun set(index: Int, zIndex: ZIndex) {
    get(index).also {
      it.index = index
      it.zIndex = zIndex
    }
  }

  /**
   * Represents a mapping from index to z-index
   */
  class Entry : LayoutVariable, Comparable<Entry> {
    var index: Int = -1
    var zIndex: ZIndex = ZIndex.auto

    override fun compareTo(other: Entry): Int {
      return this.zIndex.compareTo(other.zIndex)
    }

    override fun reset() {
      index = -1
      zIndex = ZIndex.auto
    }

    override fun toString(): String {
      return "Entry(index=$index, zIndex=$zIndex)"
    }

    companion object {
      /**
       * Comparator to sort the entries by z index
       */
      val byZIndex: Comparator<Entry> = Comparator { a, b -> a.zIndex.compareTo(b.zIndex) }
    }
  }
}

