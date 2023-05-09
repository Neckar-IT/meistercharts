package it.neckar.open.provider

import it.neckar.open.annotations.CreatesObjects
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
@CreatesObjects
fun <T> SizedProvider<T>.filtered(filter: MultiProviderFilter<T>): FilteredSizedProvider<T> {
  return FilteredSizedProvider(this, filter)
}

