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

import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.AxisOrientationX
import it.neckar.geometry.AxisOrientationY
import it.neckar.geometry.AxisSelection
import it.neckar.geometry.Distance
import com.meistercharts.model.Insets
import it.neckar.geometry.Size
import com.meistercharts.model.Zoom
import it.neckar.open.collections.fastForEach
import it.neckar.open.observable.ObservableObject
import it.neckar.open.unit.number.MayBeZero
import it.neckar.open.unit.other.px

/**
 * Default implementation for mutable chart state
 */
open class DefaultChartState : AbstractChartState(), ObservableChartState {

  @px
  final override val zoomProperty: ObservableObject<Zoom> = ObservableObject(Zoom.default)

  override var zoom: Zoom by zoomProperty

  /**
   * The translation of the window on the y-axis
   */
  final override val windowTranslationProperty: @Zoomed ObservableObject<Distance> = ObservableObject(Distance.none)

  override var windowTranslation: @Zoomed Distance by windowTranslationProperty


  @ContentArea
  @px
  final override val contentAreaSizeProperty: ObservableObject<@ContentArea @MayBeZero Size> = ObservableObject(Size.zero).also {
    it.consume { newSize ->
      require(newSize.bothNotNegative()) { "Invalid content area size: $newSize" }
    }
  }

  @ContentArea
  @px
  override var contentAreaSize: Size by contentAreaSizeProperty

  @Zoomed
  @px
  final override val windowSizeProperty: ObservableObject<@Zoomed @MayBeZero Size> = ObservableObject(Size.zero).also {
    it.consume { newSize ->
      require(newSize.bothNotNegative()) { "Invalid window size: $newSize" }
    }
  }

  @Zoomed
  @px
  override var windowSize: @MayBeZero @Zoomed Size by windowSizeProperty


  final override val contentViewportMarginProperty: ObservableObject<@Zoomed Insets> = ObservableObject(Insets.empty)

  override var contentViewportMargin: Insets by contentViewportMarginProperty


  final override val axisOrientationXProperty: ObservableObject<AxisOrientationX> = ObservableObject(AxisOrientationX.OriginAtLeft).also {
    it.consume { newValue ->
      axisOrientationX = newValue
    }
  }

  //This is some kind of performance optimization
  override var axisOrientationX: AxisOrientationX = axisOrientationXProperty.get()
    set(value) {
      field = value
      axisOrientationXProperty.value = value
    }

  final override val axisOrientationYProperty: ObservableObject<AxisOrientationY> = ObservableObject(AxisOrientationY.OriginAtBottom)

  override var axisOrientationY: AxisOrientationY by axisOrientationYProperty


  init {
    zoomProperty.consume { notifyListeners() }
    windowTranslationProperty.consume { notifyListeners() }
    contentAreaSizeProperty.consume { notifyListeners() }
    windowSizeProperty.consume { notifyListeners() }
    axisOrientationXProperty.consume { notifyListeners() }
    axisOrientationYProperty.consume { notifyListeners() }
  }

  private val changeListeners = mutableListOf<(chartState: ObservableChartState) -> Unit>()

  /**
   * Register a lambda that is called whenever the state of the chart state is updated.
   * If only changes to some properties are required, register at these properties directly
   */
  override fun onChange(listener: (chartState: ObservableChartState) -> Unit) {
    changeListeners.add(listener)
  }

  override fun unregisterOnChange(listener: (chartState: ObservableChartState) -> Unit) {
    changeListeners.remove(listener)
  }

  //property: KProperty<*>, oldValue: T, newValue: T
  private fun notifyListeners() {
    changeListeners.fastForEach {
      it(this)
    }
  }

  /**
   * Binds the properties bidirectional to the other state.
   *
   * This method may be used to synchronize two chart states of two different chart canvas objects.
   * Therefore, they zoom and pan synchronized.
   */
  override fun bindBidirectional(otherState: MutableChartState, axisSelection: AxisSelection) {
    when (axisSelection) {
      AxisSelection.None -> return
      AxisSelection.Both -> {
        zoomProperty.bindBidirectional(otherState.zoomProperty)
        windowTranslationProperty.bindBidirectional(otherState.windowTranslationProperty)
        axisOrientationXProperty.bindBidirectional(otherState.axisOrientationXProperty)
        axisOrientationYProperty.bindBidirectional(otherState.axisOrientationYProperty)
      }

      AxisSelection.X -> {
        zoomProperty.bindBidirectional(otherState.zoomProperty,
          { newValueToConvert, oldConvertedValue -> oldConvertedValue.withX(newValueToConvert.scaleX) },
          { newValueToConvert, oldConvertedValue -> oldConvertedValue.withX(newValueToConvert.scaleX) }
        )
        windowTranslationProperty.bindBidirectional(otherState.windowTranslationProperty,
          { newValueToConvert, oldConvertedValue -> oldConvertedValue.withX(newValueToConvert.x) },
          { newValueToConvert, oldConvertedValue -> oldConvertedValue.withX(newValueToConvert.x) }
        )
        axisOrientationXProperty.bindBidirectional(otherState.axisOrientationXProperty)
      }

      AxisSelection.Y -> {
        zoomProperty.bindBidirectional(otherState.zoomProperty,
          { newValueToConvert, oldConvertedValue -> oldConvertedValue.withY(newValueToConvert.scaleY) },
          { newValueToConvert, oldConvertedValue -> oldConvertedValue.withY(newValueToConvert.scaleY) }
        )
        windowTranslationProperty.bindBidirectional(otherState.windowTranslationProperty,
          { newValueToConvert, oldConvertedValue -> oldConvertedValue.withY(newValueToConvert.y) },
          { newValueToConvert, oldConvertedValue -> oldConvertedValue.withY(newValueToConvert.y) }
        )
        axisOrientationYProperty.bindBidirectional(otherState.axisOrientationYProperty)
      }
    }

    //Do *NOT* bind the canvas size - which is different for each chart
  }
}
