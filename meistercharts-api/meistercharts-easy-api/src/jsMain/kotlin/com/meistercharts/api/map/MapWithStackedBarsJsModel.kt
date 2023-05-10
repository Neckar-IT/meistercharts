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
package com.meistercharts.api.map

import com.meistercharts.api.ValueRange
import it.neckar.open.unit.other.deg

/**
 * The data of a map with stacked bars
 */
external interface MapWithBarsData {
  val stackedBars: Array<StackedBarOnMap?>?
}

/**
 * The data of a stacked bar on a map
 */
external interface StackedBarOnMap {
  /**
   * The latitude (90° N - 90° S) of the location of the bar
   */
  val latitude: @deg Double

  /**
   * The longitude (180° W - 180° E) of the location of the bar
   */
  val longitude: @deg Double

  /**
   * The name of the location
   */
  val locationName: String

  /**
   * The values of the stacked bar
   */
  @Suppress("ArrayPrimitive")
  val barValues: Array<Double>

  /**
   * The colors to be used for the values of the bar
   */
  val barColors: Array<String>
}

/**
 * The style of a map with stacked bars
 */
external interface MapWithBarsStyle {
  /**
   * The value range to be used
   */
  val valueRange: ValueRange?
}
