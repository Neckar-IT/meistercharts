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

import com.meistercharts.algorithms.layers.ResizablePaintableLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addFillCanvasBackground
import com.meistercharts.algorithms.layers.compass.CompassNeedlePainter
import com.meistercharts.algorithms.layers.compass.GaugePaintable
import com.meistercharts.algorithms.layers.compass.PuristicCompassPainter
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.algorithms.layers.text.TextLayer
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.design.Theme
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontSize
import com.meistercharts.model.Insets
import com.meistercharts.provider.ValueRangeProvider
import com.meistercharts.provider.delegate
import com.meistercharts.range.ValueRange
import it.neckar.geometry.Direction
import it.neckar.geometry.RotationDirection
import it.neckar.geometry.Size
import it.neckar.open.formatting.decimalFormat1digit
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.observable.ObservableObject
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.asDoubleProvider
import it.neckar.open.provider.delegate
import it.neckar.open.unit.other.deg
import kotlin.math.PI

/**
 * A modern compass
 */
class PuristicCompassGestalt(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {}
) : ChartGestalt {

  constructor(
    currentValueProvider: @deg DoubleProvider = 330.0.asDoubleProvider(),
    valueRangeProvider: ValueRangeProvider = { ValueRange.degrees },
    additionalConfiguration: Configuration.() -> Unit = {}
  ): this(Configuration(currentValueProvider, valueRangeProvider), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  val fixedChartGestalt: FixedChartGestalt = FixedChartGestalt()

  val puristicCompassPainter: PuristicCompassPainter = PuristicCompassPainter()
  val compassNeedlePainter: CompassNeedlePainter = CompassNeedlePainter {
    gap = { 0.0 }
  }

  val gaugePaintable: GaugePaintable = GaugePaintable(
    configuration::valueRangeProvider.delegate(),
    configuration::currentValueProvider.delegate(),
    Size(100.0, 100.0)
  ) {
    basePainter = puristicCompassPainter
    valuePainter = compassNeedlePainter

    startAt = -PI / 2.0
    extend = 2 * PI
    rotationDirection = RotationDirection.Clockwise
  }

  val resizablePaintableLayer: ResizablePaintableLayer = ResizablePaintableLayer(Insets.empty) {
    gaugePaintable
  }

  val valueLayer: TextLayer = TextLayer({ _, i18nConfiguration ->
    val value = configuration.currentValueProvider() % 360.0
    listOf("${decimalFormat1digit.format(value, i18nConfiguration)}°")
  }) {
    anchorDirection = Direction.BottomCenter
  }

  val subValueLayer: TextLayer = TextLayer({ _, _ ->
    listOf("48°24'49.7\"N", "9°03'03.0\"E")
  }) {
    font = FontDescriptorFragment(size = FontSize(20.0)).asProvider()
    margin = Insets.of(500.0, 0.0, 0.0, 0.0)
    anchorDirection = Direction.TopCenter
  }

  override fun configure(meisterChartBuilder: MeisterchartBuilder) {
    configuration.marginProperty.consumeImmediately {
      fixedChartGestalt.contentViewportMargin = it
      resizablePaintableLayer.insets = it
    }

    fixedChartGestalt.configure(meisterChartBuilder)

    meisterChartBuilder.configure {

      //Adjust font-size in accordance with window height which is approximately the diameter of the compass.
      chartSupport.rootChartState.windowSizeProperty.consumeImmediately {
        valueLayer.configuration.font = Theme.mainValueLabelFont.provider()
        subValueLayer.configuration.font = Theme.subValueLabelFont.provider()
        subValueLayer.configuration.margin = Insets.onlyTop(it.height * 0.5)
      }

      layers.addClearBackground()
      layers.addFillCanvasBackground()
      this.layers.addLayer(resizablePaintableLayer)
      this.layers.addLayer(valueLayer)
      this.layers.addLayer(subValueLayer)

      layers.addVersionNumberHidden()
    }
  }

  @ConfigurationDsl
  class Configuration(
    /**
     * Provides the current value of the compass
     */
    var currentValueProvider: @deg DoubleProvider = 330.0.asDoubleProvider(),
    var valueRangeProvider: ValueRangeProvider = { ValueRange.degrees },
  ) {
    val marginProperty: ObservableObject<Insets> = ObservableObject(Insets.of(5.0))

    /**
     * The  margin around the chart
     */
    var margin: Insets by marginProperty
  }
}
