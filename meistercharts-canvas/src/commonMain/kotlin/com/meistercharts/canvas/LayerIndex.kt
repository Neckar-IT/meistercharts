package com.meistercharts.canvas

import kotlin.jvm.JvmInline

/**
 * Represents the index of a layer (during layout or paint).
 * The index can be used to determine the order of the layers.
 */
@JvmInline
value class LayerIndex(val value: Int) : Comparable<Int> {
  override fun toString(): String {
    return "$value"
  }

  override fun compareTo(other: Int): Int {
    return value.compareTo(other)
  }

  fun compareTo(other: LayerIndex): Int {
    return value.compareTo(other.value)
  }

  companion object {
    /**
     * Represents an unknown layer index.
     * Must not be used during layout/painting of a layer
     */
    val unknown: LayerIndex = LayerIndex(-1)
  }
}

