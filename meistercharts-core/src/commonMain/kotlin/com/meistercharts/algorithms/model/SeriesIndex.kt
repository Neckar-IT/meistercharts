package com.meistercharts.algorithms.model

import it.neckar.open.provider.MultiProvider
import kotlin.jvm.JvmInline

/**
 *
 */
@JvmInline
value class SeriesIndex(val value: Int) {

  companion object {
    val zero: SeriesIndex = SeriesIndex(0)
    val one: SeriesIndex = SeriesIndex(1)
  }
}

inline fun <T> MultiProvider<SeriesIndex, T>.valueAt(index: SeriesIndex): T {
  return this.valueAt(index.value)
}
