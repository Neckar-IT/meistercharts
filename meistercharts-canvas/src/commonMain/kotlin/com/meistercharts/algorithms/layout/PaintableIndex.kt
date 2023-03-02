package com.meistercharts.algorithms.layout

import it.neckar.open.provider.MultiProvider
import kotlin.jvm.JvmInline

/**
 * Identifies the paintable using the index
 */
@JvmInline
value class PaintableIndex(val value: Int)

inline fun <T> MultiProvider<PaintableIndex, T>.valueAt(index: PaintableIndex): T {
  return this.valueAt(index.value)
}
