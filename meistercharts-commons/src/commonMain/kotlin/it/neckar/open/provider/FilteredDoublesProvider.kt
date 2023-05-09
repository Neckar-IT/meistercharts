package it.neckar.open.provider

import it.neckar.open.annotations.CreatesObjects
import it.neckar.open.collections.emptyIntArray
import it.neckar.open.kotlin.lang.DoublesFilter
import it.neckar.open.kotlin.lang.fastFor

/**
 * Filters the doubles.
 *
 * Contract: Every time when [size] is called, the values are recalculated
 */
class FilteredDoublesProvider(
  /**
   * The delegate
   */
  val delegate: DoublesProvider,
  /**
   * Is used to filter the doubles
   */
  private val filter: DoublesFilter,
) : DoublesProvider, IndexMapping {

  /**
   * Contains the original indices that have been filtered
   */
  private var filteredOriginalIndices: IntArray = emptyIntArray()

  /**
   * The effective size of the [filteredOriginalIndices] array.
   */
  private var filteredOriginalIndicesEffectiveSize: Int = 0

  /**
   * Calling size triggers reevaluation of the filtering
   */
  override fun size(): Int {
    val delegateSize = delegate.size()

    //Check if the size might have changed!
    if (filteredOriginalIndices.size != delegateSize) {
      filteredOriginalIndices = IntArray(delegateSize)
    }

    //Fill the values
    var targetIndex = 0
    delegateSize.fastFor { originalIndex ->
      val value = delegate.valueAt(originalIndex)

      //Check if the value is visible
      val visible = filter.filter(value)

      if (visible) {
        filteredOriginalIndices[targetIndex] = originalIndex
        targetIndex++
      }
    }

    filteredOriginalIndicesEffectiveSize = targetIndex
    return filteredOriginalIndicesEffectiveSize
  }

  override fun mapped2Original(mappedIndex: Int): Int {
    return filteredOriginalIndices[mappedIndex]
  }

  override fun valueAt(index: Int): Double {
    val originalIndex = mapped2Original(index)
    return delegate.valueAt(originalIndex)
  }

  /**
   * Wraps the given multi provider
   */
  fun <IndexContextOld, IndexContextNew, T> wrapMultiProvider(delegate: MultiProvider<IndexContextOld, T>): MultiProvider<IndexContextNew, T> {
    return MappedIndexMultiProvider(delegate, this)
  }
}

/**
 * Wraps this sorted provider within a sorted [DoublesProvider].
 *
 * Attention: A new object is created!
 */
@CreatesObjects
fun DoublesProvider.filtered(filter: DoublesFilter): FilteredDoublesProvider {
  return FilteredDoublesProvider(this, filter)
}

/**
 * Returns only the finite values
 */
@CreatesObjects
fun DoublesProvider.filteredOnlyFinite(): FilteredDoublesProvider {
  return filtered(DoublesFilter.finite)
}

