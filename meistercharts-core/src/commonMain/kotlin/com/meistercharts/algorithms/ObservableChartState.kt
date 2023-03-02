package com.meistercharts.algorithms

import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.annotations.ContentArea
import it.neckar.open.unit.number.MayBeZero
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Distance
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
