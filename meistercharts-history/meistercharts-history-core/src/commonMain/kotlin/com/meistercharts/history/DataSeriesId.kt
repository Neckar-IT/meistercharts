package com.meistercharts.history

import kotlin.jvm.JvmInline

/**
 * Represents the ID for a data series
 *
 * Attention: This is not the same as a [com.meistercharts.history.impl.EnumDataSeriesIndex] / [com.meistercharts.history.impl.DecimalDataSeriesIndex]
 */
@JvmInline
value class DataSeriesId(val value: Int) {
  override fun toString(): String {
    return value.toString()
  }
}
