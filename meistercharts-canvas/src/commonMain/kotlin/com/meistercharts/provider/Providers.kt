package com.meistercharts.provider

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.Limit
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Insets
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.formatting.NumberFormat
import it.neckar.open.unit.other.deg
import kotlin.reflect.KProperty0

/**
 * Provides the value range
 */
typealias ValueRangeProvider = () -> ValueRange
/**
 * Provides the time range
 */
typealias TimeRangeProvider = () -> TimeRange
/**
 * Returns coordinates
 */
typealias CoordinatesProvider = () -> @Domain Coordinates
/**
 * Provides insets
 */
typealias InsetsProvider = () -> Insets
/**
 * Provides a color
 */
typealias ColorProvider = () -> Color
/**
 * Provides a [NumberFormat]
 */
typealias NumberFormatProvider = () -> NumberFormat

/**
 * Provides the limits
 */
typealias LimitsProvider = SizedProvider<Limit>

/**
 * Provides values for compasses
 */
typealias CompassValueProvider = @deg DoubleProvider

/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun <T> KProperty0<() -> T>.delegate(): () -> T {
  return {
    get()()
  }
}
