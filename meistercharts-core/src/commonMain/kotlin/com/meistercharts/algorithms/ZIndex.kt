package com.meistercharts.algorithms

import it.neckar.open.unit.number.PositiveOrZero
import kotlin.jvm.JvmInline

/**
 * Represents the Z-Index.
 *
 * * High values: Placed at the top
 * * Low values: Placed at the bottom
 */
@com.meistercharts.annotations.ZIndex
@JvmInline
value class ZIndex(val value: @PositiveOrZero Double) : Comparable<ZIndex> {
  override fun compareTo(other: ZIndex): Int {
    return this.value.compareTo(other.value)
  }

  companion object {
    /**
     * Represents the "lowest" z-index that is assigned by default
     */
    val auto: ZIndex = ZIndex(0.0)
  }
}
