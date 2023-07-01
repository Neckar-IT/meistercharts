/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.provider

import com.meistercharts.time.TimeRange
import com.meistercharts.range.ValueRange
import com.meistercharts.algorithms.layers.Limit
import com.meistercharts.color.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.geometry.Coordinates
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
