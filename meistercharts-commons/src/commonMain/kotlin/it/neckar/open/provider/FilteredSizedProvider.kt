/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.provider

import it.neckar.open.annotations.Allocates
import it.neckar.open.annotations.AllocationCost
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.impl.IndexMappingSupport

typealias MultiProviderFilter<T> = (index: Int, value: T) -> Boolean

/**
 * Filters the doubles.
 *
 * Contract: Every time when [size] is called, the values are recalculated
 */
class FilteredSizedProvider<T>(
  /**
   * The delegate
   */
  delegate: SizedProvider<T>,

  /**
   * Is called for each value
   */
  val filter: MultiProviderFilter<T>,
) : AbstractMappedSizedProvider<T>(delegate), SizedProvider<T>, IndexMapping {

  override fun IndexMappingSupport.StoreIndex.fillIndexMapping(delegateSize: Int): Int {
    //Fill the index mapping
    var targetIndex = 0
    delegateSize.fastFor { originalIndex ->
      val value = delegate.valueAt(originalIndex)

      //Check if the value is visible
      val visible = filter(originalIndex, value)

      if (visible) {
        this.storeMapping(originalIndex, targetIndex)
        targetIndex++
      }
    }
    return targetIndex
  }
}

/**
 * Wraps this sorted provider within a sorted [DoublesProvider].
 *
 * Attention: A new object is created!
 */
@Allocates(AllocationCost.Constant)
fun <T> SizedProvider<T>.filtered(filter: MultiProviderFilter<T>): FilteredSizedProvider<T> {
  return FilteredSizedProvider(this, filter)
}

