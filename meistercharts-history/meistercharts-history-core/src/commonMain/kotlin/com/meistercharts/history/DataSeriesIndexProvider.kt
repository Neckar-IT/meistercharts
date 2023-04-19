package com.meistercharts.history

import it.neckar.open.provider.HasSize
import it.neckar.open.provider.IntProvider
import kotlin.reflect.KProperty0

/**
 * Provides data series indices
 */
interface DataSeriesIndexProvider<out DataSeriesIndexType : DataSeriesIndex> : HasSize {
  //This class has been checked - it does *not* box.
  /**
   * Returns the data series index.
   */
  fun valueAt(index: Int): DataSeriesIndexType
}

/**
 * Returns a new instance of [DecimalDataSeriesIndexProvider] that has a size that is not greater than the provided value
 */
fun <DataSeriesIndexType : DataSeriesIndex> DataSeriesIndexProvider<DataSeriesIndexType>.atMost(maxSizeProvider: IntProvider): DataSeriesIndexProvider<DataSeriesIndexType> {
  val delegateProvider = { this }
  return atMost(delegateProvider, maxSizeProvider)
}

/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun <DataSeriesIndexType : DataSeriesIndex> KProperty0<DataSeriesIndexProvider<DataSeriesIndexType>>.atMost(maxSizeProvider: IntProvider): DataSeriesIndexProvider<DataSeriesIndexType> {
  return atMost({ get() }, maxSizeProvider)
}

private fun <DataSeriesIndexType : DataSeriesIndex> atMost(delegateProvider: () -> DataSeriesIndexProvider<DataSeriesIndexType>, maxSizeProvider: IntProvider): DelegatingDataSeriesIndexProvider<DataSeriesIndexType> {
  return object : DelegatingDataSeriesIndexProvider<DataSeriesIndexType>(delegateProvider) {
    override fun size(): Int {
      return super.size().coerceAtMost(maxSizeProvider())
    }
  }
}

open class DelegatingDataSeriesIndexProvider<out DataSeriesIndexType : DataSeriesIndex>(
  val delegateProvider: () -> DataSeriesIndexProvider<DataSeriesIndexType>,
) : DataSeriesIndexProvider<DataSeriesIndexType> {
  override fun size(): Int = delegateProvider().size()

  override fun valueAt(index: Int): DataSeriesIndexType {
    return delegateProvider().valueAt(index)
  }
}

/**
 * Iterates over the provided indices.
 * This method is only useful in very few cases!
 *
 * ATTENTION: "i" is very different to the provided index
 */
inline fun <DataSeriesIndexType : DataSeriesIndex> DataSeriesIndexProvider<DataSeriesIndexType>.fastForEachIndexed(callback: (i: Int, value: DataSeriesIndexType) -> Unit) {
  val currentSize = size()
  fastForEachIndexed(currentSize, callback)
}

/**
 * Iterates over the provided indices - but with the provided max size
 */
inline fun <DataSeriesIndexType : DataSeriesIndex> DataSeriesIndexProvider<DataSeriesIndexType>.fastForEachIndexed(maxSize: Int, callback: (i: Int, value: DataSeriesIndexType) -> Unit) {
  val size = size().coerceAtMost(maxSize)

  var n = 0
  while (n < size) {
    callback(n, this.valueAt(n))
    n++
  }
}

inline fun <DataSeriesIndexType : DataSeriesIndex> DataSeriesIndexProvider<DataSeriesIndexType>.fastForEach(callback: (index: DataSeriesIndexType) -> Unit) {
  var n = 0
  val currentSize = size()
  while (n < currentSize) {
    callback(this.valueAt(n++))
  }
}
