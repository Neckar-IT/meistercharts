package com.meistercharts.algorithms

import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.annotations.ContentArea
import it.neckar.open.unit.number.MayBeZero
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Distance
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom
import it.neckar.open.observable.ObservableObject

/**
 * Contains the setter for the chart state
 *
 */
interface MutableChartState : ObservableChartState {
  override var axisOrientationX: AxisOrientationX

  override var axisOrientationY: AxisOrientationY


  override var windowTranslation: @Zoomed @MayBeZero Distance

  override var windowTranslationX: @Zoomed @MayBeZero Double

  override var windowTranslationY: @Zoomed @MayBeZero Double

  override val windowTranslationProperty: ObservableObject<@Zoomed @MayBeZero Distance>


  override var contentViewportMarginTop: @Zoomed @MayBeZero Double

  override var contentViewportMarginRight: @Zoomed @MayBeZero Double

  override var contentViewportMarginBottom: @Zoomed @MayBeZero Double

  override var contentViewportMarginLeft: @Zoomed @MayBeZero Double

  override var contentViewportMargin: @Zoomed Insets

  override val contentViewportMarginProperty: ObservableObject<@Zoomed Insets>


  override var zoomX: Double

  override var zoomY: Double

  override var zoom: Zoom

  override val zoomProperty: ObservableObject<Zoom>


  override var contentAreaWidth: @ContentArea @MayBeZero Double

  override var contentAreaHeight: @ContentArea @MayBeZero Double

  override var contentAreaSize: @ContentArea @MayBeZero Size

  override val contentAreaSizeProperty: ObservableObject<@ContentArea @MayBeZero Size>


  override var windowWidth: @Zoomed @MayBeZero Double

  override var windowHeight: @Zoomed @MayBeZero Double

  override var windowSize: @Zoomed @MayBeZero Size

  override val windowSizeProperty: ObservableObject<@Zoomed @MayBeZero Size>


  override val axisOrientationXProperty: ObservableObject<AxisOrientationX>

  override val axisOrientationYProperty: ObservableObject<AxisOrientationY>


  /**
   * Binds the properties bidirectional to the other state.
   *
   * This method may be used to synchronize two chart states of two different chart canvas objects.
   * Therefore, they zoom and pan synchronized.
   */
  fun bindBidirectional(otherState: MutableChartState, axisSelection: AxisSelection)
}
