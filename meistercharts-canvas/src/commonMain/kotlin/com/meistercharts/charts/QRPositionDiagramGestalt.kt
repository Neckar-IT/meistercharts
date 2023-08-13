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
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.ContentAreaLayer
import com.meistercharts.algorithms.layers.DomainAxisMarkersLayer
import com.meistercharts.algorithms.layers.DomainRelativeGridLayer
import com.meistercharts.algorithms.layers.PaintableTranslateRotateLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.createGrid
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.color.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.RectanglePaintable
import it.neckar.geometry.Coordinates
import com.meistercharts.model.Insets
import it.neckar.geometry.Side
import it.neckar.geometry.Size
import com.meistercharts.model.Vicinity
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.asProvider1
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableDouble
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ObservableString
import it.neckar.open.unit.si.rad
import kotlin.jvm.JvmOverloads

class QRPositionDiagramGestalt @JvmOverloads constructor(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {},
) : ChartGestalt {

  val style: Style = Style().also(styleConfiguration)

  val fixedChartGestalt: FixedChartGestalt = FixedChartGestalt()

  val valueAxisXLayer: ValueAxisLayer = ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { data.valueRangeX })) {
    titleProvider = { _, _ -> data.xAxisCaption }
    tickOrientation = Vicinity.Outside
    paintRange = AxisConfiguration.PaintRange.Continuous
    side = Side.Bottom
    titleColor = Color.black.asProvider()
    lineColor = Color.black.asProvider()
    tickLabelColor = Color.black.asProvider()
    margin = Insets.empty
  }

  val gridLayerX: DomainRelativeGridLayer = valueAxisXLayer.createGrid {
    lineStyles = LineStyle(color = Color.lightgray, lineWidth = 0.5).asProvider1()
  }

  val valueAxisYLayer: ValueAxisLayer = ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { data.valueRangeY })) {
    titleProvider = { _, _ -> data.yAxisCaption }
    tickOrientation = Vicinity.Outside
    paintRange = AxisConfiguration.PaintRange.Continuous
    side = Side.Left
    titleColor = Color.black.asProvider()
    lineColor = Color.black.asProvider()
    tickLabelColor = Color.black.asProvider()
    margin = Insets.empty
  }

  val gridLayerY: DomainRelativeGridLayer = valueAxisYLayer.createGrid {
    lineStyles = LineStyle(color = Color.lightgray, lineWidth = 0.5).asProvider1()
  }

  val paintableTranslateRotateLayer: PaintableTranslateRotateLayer = PaintableTranslateRotateLayer(
    image = { style.image },
    x = { data.xDomainRelative },
    y = { data.yDomainRelative },
    angle = { data.angle }
  )

  val domainAxisMarkersLayer: DomainAxisMarkersLayer = DomainAxisMarkersLayer({ Coordinates(data.valueRangeX.toDomainRelative(data.x), data.valueRangeY.toDomainRelative(data.y)) })

  init {
    style.marginProperty.consumeImmediately {
      fixedChartGestalt.contentViewportMargin = it

      valueAxisXLayer.axisConfiguration.size = it.bottom
      gridLayerX.configuration.passpartout = it

      valueAxisYLayer.axisConfiguration.size = it.left

      gridLayerY.configuration.passpartout = it
    }
  }

  override fun configure(meisterChartBuilder: MeisterchartBuilder) {
    meisterChartBuilder.apply {
      fixedChartGestalt.configure(this)

      configure {
        layers.addClearBackground()
        chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtBottom

        layers.addLayer(ContentAreaLayer())

        layers.addLayer(valueAxisXLayer)
        layers.addLayer(gridLayerX)
        layers.addLayer(valueAxisYLayer)
        layers.addLayer(gridLayerY)

        layers.addLayer(paintableTranslateRotateLayer)

        layers.addLayer(domainAxisMarkersLayer.visibleIf(style.paintDomainAxisMarkersProperty))

        layers.addVersionNumberHidden()
      }
    }
  }

  class Data {
    val valueRangeXProperty: ObservableObject<ValueRange> = ObservableObject(ValueRange.default)

    /**
     * The value range in X direction
     */
    var valueRangeX: ValueRange by valueRangeXProperty

    val valueRangeYProperty: ObservableObject<ValueRange> = ObservableObject(ValueRange.default)

    /**
     * The value range in Y direction
     */
    var valueRangeY: ValueRange by valueRangeYProperty

    val xAxisCaptionProperty: ObservableString = ObservableString("")

    /**
     * X axis caption
     */
    var xAxisCaption: String by xAxisCaptionProperty

    val yAxisCaptionProperty: ObservableString = ObservableString("")

    /**
     * Y axis caption
     */
    var yAxisCaption: String by yAxisCaptionProperty

    val xProperty: @Domain ObservableDouble = ObservableDouble(0.0)

    /**
     * The X coordinate to paint the QR code in
     */
    var x: @Domain Double by xProperty

    val xDomainRelative: @DomainRelative Double
      get() {
        return valueRangeX.toDomainRelative(x)
      }

    val yProperty: @Domain ObservableDouble = ObservableDouble(0.0)

    /**
     * The Y coordinate to paint the QR code in
     */
    var y: @Domain Double by yProperty

    val yDomainRelative: @DomainRelative Double
      get() {
        return valueRangeY.toDomainRelative(y)
      }

    /**
     * The coordinates
     */
    val coordinates: @Domain Coordinates
      get() {
        return Coordinates(x, y)
      }

    val angleProperty: @rad ObservableDouble = ObservableDouble(0.0)

    /**
     * The angle to paint the QR code in
     */
    var angle: @rad Double by angleProperty
  }

  @ConfigurationDsl
  class Style {
    val marginProperty: ObservableObject<Insets> = ObservableObject(Insets(50.0, 50.0, 50.0, 70.0))

    /**
     * The  margin around the chart
     */
    var margin: Insets by marginProperty

    /**
     * The image to be painted
     */
    var image: Paintable = RectanglePaintable(Size.PX_90, Color.red)


    val paintDomainAxisMarkersProperty: ObservableBoolean = ObservableBoolean(true)

    /**
     * whether to paint the domain axis markers
     */
    var paintDomainAxisMarkers: Boolean by paintDomainAxisMarkersProperty
  }
}

