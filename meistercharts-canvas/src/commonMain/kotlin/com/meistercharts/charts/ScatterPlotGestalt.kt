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
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.DomainRelativeGridLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.createGrid
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.scatterplot.ScatterPlotLayer
import com.meistercharts.color.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.model.Insets
import it.neckar.geometry.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.provider.DoublesProvider
import com.meistercharts.provider.ValueRangeProvider
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.kotlin.lang.randomNormal
import it.neckar.open.kotlin.lang.asProvider1
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.observable.ObservableObject

/**
 * A scatter plot that paints values with x/y values
 */
class ScatterPlotGestalt(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : ChartGestalt {

  constructor(
    xValues: @Domain DoublesProvider,
    yValues: @Domain DoublesProvider,
    valueRangeX: ValueRangeProvider,
    valueRangeY: ValueRangeProvider,
    valueAxisCaptionX: String = "Demo Data X",
    valueAxisCaptionY: String = "Demo Data Y",
    additionalConfiguration: Configuration.() -> Unit = {},
  ) : this(Configuration(xValues, yValues, valueRangeX, valueRangeY, valueAxisCaptionX, valueAxisCaptionY), additionalConfiguration)

  val fixedChartGestalt: FixedChartGestalt = FixedChartGestalt(
    Insets(50.0, 50.0, 50.0, 70.0)
  )

  val valueAxisXLayer: ValueAxisLayer = ValueAxisLayer(
    ValueAxisLayer.Configuration(
      valueRangeProvider = configuration.valueRangeXProvider
    )
  ) {
    titleProvider = { _, _ -> configuration.valueAxisCaptionX }
    tickOrientation = Vicinity.Outside
    paintRange = AxisConfiguration.PaintRange.Continuous
    side = Side.Bottom
  }

  val valueAxisYLayer: ValueAxisLayer = ValueAxisLayer(
    ValueAxisLayer.Configuration(
      valueRangeProvider = configuration.valueRangeYProvider
    )
  ) {
    titleProvider = { _, _ -> configuration.valueAxisCaptionY }
    tickOrientation = Vicinity.Outside
    paintRange = AxisConfiguration.PaintRange.Continuous
    side = Side.Left
  }

  val gridXLayer: DomainRelativeGridLayer = valueAxisXLayer.createGrid {
    lineStyles = LineStyle(color = Color.lightgray, lineWidth = 0.5).asProvider1()
  }
  val gridYLayer: DomainRelativeGridLayer = valueAxisYLayer.createGrid {
    lineStyles = LineStyle(color = Color.lightgray, lineWidth = 0.5).asProvider1()
  }

  val scatterPlotLayer: ScatterPlotLayer = ScatterPlotLayer(configuration)

  init {
    configuration.additionalConfiguration()

    configuration.marginProperty.consumeImmediately {
      fixedChartGestalt.contentViewportMargin = it

      valueAxisXLayer.configuration.size = it.bottom
      valueAxisYLayer.configuration.size = it.left

      gridXLayer.configuration.passpartout = it
      gridYLayer.configuration.passpartout = it
    }
  }

  override fun configure(meisterChartBuilder: MeisterchartBuilder) {
    fixedChartGestalt.configure(meisterChartBuilder)

    meisterChartBuilder.configure {
      layers.addClearBackground()

      layers.addLayer(scatterPlotLayer)
      layers.addLayer(valueAxisXLayer) //TODO bind visiblity
      layers.addLayer(valueAxisYLayer) //TODO bind visiblity
      layers.addLayer(gridXLayer)
      layers.addLayer(gridYLayer)

      layers.addVersionNumberHidden()
    }
  }

  @ConfigurationDsl
  class Configuration(
    xValues: @Domain DoublesProvider,
    yValues: @Domain DoublesProvider,
    valueRangeX: ValueRangeProvider,
    valueRangeY: ValueRangeProvider,
    /**
     * The caption of the X value axis
     */
    //TODO i18n
    val valueAxisCaptionX: String = "Demo Data X",

    /**
     * The caption of the Y value axis
     */
    //TODO i18n
    val valueAxisCaptionY: String = "Demo Data Y",
  ) : ScatterPlotLayer.Configuration(xValues, yValues, valueRangeX, valueRangeY) {
    /**
     * The margin of the chart
     */
    val marginProperty: ObservableObject<@Zoomed Insets> = ObservableObject(Insets.of(30.0, 30.0, 50.0, 80.0))
    var margin: @Zoomed Insets by marginProperty
  }

  companion object {
    fun createDefaultData(
      valueRangeX: ValueRange = ValueRange.linear(0.0, 100.0),
      valueRangeY: ValueRange = ValueRange.linear(0.0, 100.0),
    ): Configuration {
      val xValues = DoubleArrayList()
      val yValues = DoubleArrayList()

      // fill model with demo data
      for (cloudIndex in 0 until 4) {
        val centerX = if (cloudIndex < 2) 25.0 else 75.0
        val centerY = if (cloudIndex % 2 == 0) 25.0 else 75.0

        100.fastFor {
          xValues.add(randomNormal(centerX, 12.0))
          yValues.add(randomNormal(centerY, 12.0))
        }
      }

      return Configuration(
        DoublesProvider.forValues(*xValues.data),
        DoublesProvider.forValues(*yValues.data),
        {
          valueRangeX
        }, {
          valueRangeY
        }
      )
    }
  }
}
