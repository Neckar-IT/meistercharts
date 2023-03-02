package com.meistercharts.algorithms.model

import it.neckar.open.provider.MultiDoublesProvider
import it.neckar.open.provider.MultiProvider
import kotlin.jvm.JvmInline

/**
 *
 */
@JvmInline
value class CategoryIndex(val value: Int) {
  val isFirst: Boolean
    get() {
      return value == 0
    }

  fun isEqual(other: Int): Boolean {
    return value == other
  }

  companion object {
    val zero: CategoryIndex = CategoryIndex(0)
    val one: CategoryIndex = CategoryIndex(1)
  }
}

inline fun <T> MultiProvider<CategoryIndex, T>.valueAt(index: CategoryIndex): T {
  return this.valueAt(index.value)
}

inline fun MultiDoublesProvider<CategoryIndex>.valueAt(index: CategoryIndex): Double {
  return this.valueAt(index.value)
}
