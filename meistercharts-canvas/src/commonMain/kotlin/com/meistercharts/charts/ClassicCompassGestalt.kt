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

import com.meistercharts.range.ValueRange
import com.meistercharts.algorithms.layers.ResizablePaintableLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.compass.ArrowValuePainter
import com.meistercharts.algorithms.layers.compass.CompassBasePainter
import com.meistercharts.algorithms.layers.compass.GaugePaintable
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import com.meistercharts.provider.ValueRangeProvider
import com.meistercharts.provider.delegate
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.asDoubleProvider
import it.neckar.open.provider.delegate
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.observable.ObservableObject
import it.neckar.open.unit.other.deg
import kotlin.math.PI

class ClassicCompassGestalt(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {}
) : ChartGestalt {
  val style: Style = Style().also(styleConfiguration)

  val fixedChartGestalt: FixedChartGestalt = FixedChartGestalt()

  val gaugePaintable: GaugePaintable = GaugePaintable(
    data::valueRangeProvider.delegate(),
    data::currentValueProvider.delegate(),
    Size(100.0, 100.0)
  ) {
    basePainter = CompassBasePainter()
    valuePainter = ArrowValuePainter()

    startAt = 3.0 * PI / 4.0
    extend = 3.0 * PI / 2.0
  }

  val gaugePaintableLayer: ResizablePaintableLayer = ResizablePaintableLayer(Insets.empty) {
    gaugePaintable
  }

  init {
    style.marginProperty.consumeImmediately {
      fixedChartGestalt.contentViewportMargin = it
      gaugePaintableLayer.insets = it
    }
  }

  override fun configure(meisterChartBuilder: MeisterchartBuilder) {
    fixedChartGestalt.configure(meisterChartBuilder)


    meisterChartBuilder.configure {
      layers.addClearBackground()
      layers.addLayer(gaugePaintableLayer)

      layers.addVersionNumberHidden()
    }
  }

  class Data(
    /**
     * Returns the value range
     */
    val valueRangeProvider: ValueRangeProvider = ValueRange.default.asProvider(),
    /**
     * Returns the current value
     */
    val currentValueProvider: @deg DoubleProvider = 10.0.asDoubleProvider(),
  )

  @ConfigurationDsl
  class Style {
    val marginProperty: ObservableObject<Insets> = ObservableObject(Insets.of(50.0, 50.0, 50.0, 50.0))

    /**
     * The  margin around the chart
     */
    var margin: Insets by marginProperty
  }

}
