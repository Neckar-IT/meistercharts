package com.meistercharts.canvas

import kotlin.jvm.JvmInline

/**
 * Identifies the paint loop index.
 *
 * Attention: This variable *will* overflow (from [Int.MAX_VALUE] to `0`.
 * Please do not use the value to do calculations.
 *
 * Assume that the index will overflow after about 414 days (when painting with 60 fps).
 */
@JvmInline
value class PaintingLoopIndex(val value: Int) {
  /**
   * Returns the next painting index.
   * ATTENTION: The value will overflow
   */
  fun next(): PaintingLoopIndex {
    if (value == Int.MAX_VALUE) {
      //Overflow to 0 (not Int.MIN_VALUE)
      return PaintingLoopIndex(0)
    }
    val newIndex = value + 1
    require(newIndex >= 0) { "Invalid new index $newIndex for old value $value" }
    return PaintingLoopIndex(newIndex)
  }

  override fun toString(): String {
    return "$value"
  }

  companion object {
    /**
     * Specifies a loop index that will not happen naturally.
     * Can be used to specify an unknown index.
     */
    val Unknown: PaintingLoopIndex = PaintingLoopIndex(-1)
  }
}
