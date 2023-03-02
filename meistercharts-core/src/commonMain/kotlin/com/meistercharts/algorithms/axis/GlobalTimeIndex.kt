package com.meistercharts.algorithms.axis

import it.neckar.open.provider.MultiProvider
import kotlin.jvm.JvmInline

/**
 * Represents a global time index. The index is always the same - depending on the resolution.
 * This index can be used to choose a color for a tick/segment/...
 */
@JvmInline
value class GlobalTimeIndex(val value: Int)

inline fun <T> MultiProvider<GlobalTimeIndex, T>.valueAt(index: GlobalTimeIndex): T {
  return this.valueAt(index.value)
}

