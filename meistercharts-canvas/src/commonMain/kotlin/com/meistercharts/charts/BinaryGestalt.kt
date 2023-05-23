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
package com.meistercharts.charts

import com.meistercharts.algorithms.BinaryValueRange
import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.BinaryLayer
import com.meistercharts.algorithms.layers.BinaryTicksProvider
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import it.neckar.open.provider.BooleanValuesProvider
import it.neckar.open.provider.delegate
import it.neckar.open.observable.ObservableObject

/**
 * Paints a binary value 0..1
 */
@Deprecated("probably no longer required")
class BinaryGestalt(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {}
) : ChartGestalt {

  val style: Style = Style().also(styleConfiguration)

  val withContentViewportGestalt: FitContentInViewportGestalt = FitContentInViewportGestalt()

  val valueAxisLayer: ValueAxisLayer = ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { BinaryValueRange })) {
    titleProvider = { _, _ -> "Binary [Boolean]" } //TODO extract(?)
    paintRange = AxisStyle.PaintRange.ContentArea
    tickOrientation = Vicinity.Outside
    axisEndConfiguration = AxisEndConfiguration.Exact
    ticks = BinaryTicksProvider
  }

  val binaryLayer: BinaryLayer = BinaryLayer(
    BinaryLayer.Data(
      valuesProvider = data::valuesProvider.delegate()
    )
  )

  init {
    style.marginProperty.consumeImmediately {
      withContentViewportGestalt.contentViewportMargin = it
      valueAxisLayer.style.size = it.left
    }
  }

  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    withContentViewportGestalt.configure(meisterChartBuilder)

    meisterChartBuilder.configure {
      layers.addClearBackground()
      layers.addLayer(valueAxisLayer)
      layers.addLayer(binaryLayer)

      layers.addVersionNumberHidden()
    }
  }

  class Data(
    var valuesProvider: BooleanValuesProvider = createSampleValuesProvider()
  )

  @ConfigurationDsl
  class Style {
    val marginProperty: @Zoomed ObservableObject<Insets> = (ObservableObject(Insets.of(30.0, 30.0, 50.0, 80.0)))
    var margin: Insets by marginProperty
  }

  companion object {
    fun createSampleValuesProvider(): BooleanValuesProvider {
      val availableModels: List<BooleanArray> = listOf(
        booleanArrayOf(true, false, true, true, false, true, false, false, false, true),
        booleanArrayOf(false, true, false, true, true, false, true, false, false, false, true),
        booleanArrayOf(true, false, true, false, true, true, false, true, false, false, false),
        booleanArrayOf(false, true, false, true, true, false, true, false, false, false, false)
      )

      return object : BooleanValuesProvider {
        override fun size(): Int = availableModels[0].size

        override
        fun valueAt(index: Int): Boolean {
          return availableModels[0][index]
        }
      }
    }
  }
}
