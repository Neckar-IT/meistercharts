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
package com.meistercharts.state

import com.meistercharts.axis.AxisOrientationX
import com.meistercharts.axis.AxisOrientationY
import com.meistercharts.annotations.ContentArea
import it.neckar.open.unit.number.MayBeZero
import com.meistercharts.annotations.Zoomed
import com.meistercharts.geometry.Distance
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject

/**
 * A mutable chart state that can be observed
 */
interface ObservableChartState : ChartState {
  /**
   * The current zoom
   */
  val zoomProperty: ReadOnlyObservableObject<Zoom>

  /**
   * The translation of the window on the y-axis
   */
  val windowTranslationProperty: ReadOnlyObservableObject<@Zoomed @MayBeZero Distance>

  /**
   * Provides the current window size
   */
  val windowSizeProperty: ReadOnlyObservableObject<@Zoomed @MayBeZero Size>

  /**
   * The size of the content area
   */
  val contentAreaSizeProperty: ReadOnlyObservableObject<@ContentArea @MayBeZero Size>

  /**
   * Property for the [contentViewportMargin]
   */
  val contentViewportMarginProperty: ObservableObject<@Zoomed Insets>

  /**
   * The orientation of the x-axis
   */
  val axisOrientationXProperty: ReadOnlyObservableObject<AxisOrientationX>

  /**
   * The orientation of the y-axis
   */
  val axisOrientationYProperty: ReadOnlyObservableObject<AxisOrientationY>

  /**
   * Register a lambda that is called whenever the state of the chart state is updated.
   * If only changes to some properties are required, register at these properties directly
   */
  fun onChange(listener: (chartState: ObservableChartState) -> Unit)

  /**
   * Unregisters the lambda
   */
  fun unregisterOnChange(listener: (chartState: ObservableChartState) -> Unit)
}
