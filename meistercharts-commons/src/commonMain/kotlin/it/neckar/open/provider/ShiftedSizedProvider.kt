package it.neckar.open.provider

import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.impl.IndexMappingSupport

/**
 * Filters the doubles.
 *
 * Contract: Every time when [size] is called, the values are recalculated
 */
class ShiftedSizedProvider<T>(
  delegate: SizedProvider<T>,
  private val shift: Int
) : AbstractMappedSizedProvider<T>(delegate) {

  override fun IndexMappingSupport.StoreIndex.fillIndexMapping(delegateSize: Int): Int {
    delegateSize.fastFor { originalIndex ->
      val shiftedIndex = (originalIndex + shift) % delegateSize
      this.storeMapping(originalIndex, shiftedIndex)
    }
    return delegateSize
  }
}

/**
 * Returns a new instance of a sized provider that shifts the indices by the given amount
 */
@Deprecated("Do not shift shifted providers again", level = DeprecationLevel.ERROR)
@Suppress("UNUSED_PARAMETER")
fun <T> ShiftedSizedProvider<T>.shiftedBy(shift: Int): ShiftedSizedProvider<T> {
  throw UnsupportedOperationException()
}
