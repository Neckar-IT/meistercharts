/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

