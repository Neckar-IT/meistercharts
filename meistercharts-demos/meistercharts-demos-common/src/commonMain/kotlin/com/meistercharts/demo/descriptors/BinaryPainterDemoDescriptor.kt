/*
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
package com.meistercharts.demo.descriptors

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.range.BinaryValueRange
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.zoom.FittingWithMargin
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.ContentAreaLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.BinaryPainter
import com.meistercharts.color.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.fill
import com.meistercharts.color.ColorProvider
import com.meistercharts.color.ColorProviderNullable
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorProviderNullable
import com.meistercharts.demo.configurableColorProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import it.neckar.open.kotlin.lang.asProvider

/**
 * A simple hello world demo
 */
class BinaryPainterDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("7ab59662-02c5-48b0-b8aa-eb0509ad8d00")
  override val name: String = "Binary Painter"

  //language=HTML
  override val description: String = "Shows a binary painter"
  override val category: DemoCategory = DemoCategory.Painters

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val margin = Insets(20.0, 20.0, 20.0, 70.0)

        zoomAndTranslationDefaults {
          FittingWithMargin(margin)
        }
        contentAreaSizingStrategy = BindContentAreaSize2ContentViewport()

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaLayer())
          layers.addLayer(ValueAxisLayer(ValueAxisLayer.Configuration(valueRangeProvider = { BinaryValueRange })) {
            titleProvider = { _, _ -> "Binary [Boolean]" }
            paintRange = AxisConfiguration.PaintRange.ContentArea
            size = margin.left
            tickOrientation = Vicinity.Outside
            axisEndConfiguration = AxisEndConfiguration.Exact
          })
          val painterLayer = MyBinaryPainterLayer()
          layers.addLayer(painterLayer)

          configurableList("model", painterLayer.model, painterLayer.availableModels) {
            onChange {
              painterLayer.model = it
              markAsDirty()
            }

            converter = {
              "${it.first()} - ${it.last()}"
            }
          }

          configurable("line width", painterLayer.style::lineWidth) {
            max = 20.0
            onChange {
              painterLayer.style.lineWidth = it
              markAsDirty()
            }
          }

          configurableColorProvider("stroke", painterLayer.style::stroke) {}

          configurableColorProviderNullable("shadow", painterLayer.style::shadow) {}

          configurable("shadowOffset", painterLayer.style::shadowOffset) {
            max = 20.0
          }


          configurableColorProviderNullable("areaFill", painterLayer.style::areaFill) {}
        }
      }
    }
  }
}

class MyBinaryPainterLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  /**
   * The available models
   */
  val availableModels: List<BooleanArray> = listOf(
    booleanArrayOf(true, false, true, true, false, true, false, false, false, true),
    booleanArrayOf(false, true, false, true, true, false, true, false, false, false, true),
    booleanArrayOf(true, false, true, false, true, true, false, true, false, false, false),
    booleanArrayOf(false, true, false, true, true, false, true, false, false, false, false)
  )

  var model: BooleanArray = availableModels[0]

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    gc.fill(Color.transparent)
    //Translate to the origin of the content area
    gc.translate(chartCalculator.contentAreaRelative2windowX(0.0), chartCalculator.contentAreaRelative2windowY(0.0))

    val baseLine = chartCalculator.domainRelative2zoomedY(0.0)

    val maxHeight = chartCalculator.contentAreaRelative2zoomedY(1.0)
    val maxWidth = chartCalculator.contentAreaRelative2zoomedX(1.0)

    val binaryPainter = BinaryPainter(false, false, baseLine, maxWidth, maxHeight)
    binaryPainter.lineWidth = style.lineWidth
    binaryPainter.stroke = style.stroke
    binaryPainter.shadow = style.shadow
    binaryPainter.areaFill = style.areaFill
    binaryPainter.shadowOffsetX = style.shadowOffset
    binaryPainter.shadowOffsetY = style.shadowOffset

    model.forEachIndexed { i, it ->
      @DomainRelative val domainRelativeY = BinaryValueRange.toDomainRelative(it)
      val y = chartCalculator.domainRelative2zoomedY(domainRelativeY)
      binaryPainter.addCoordinate(gc, chartCalculator.domainRelative2zoomedX(0.1 * i), y)
    }

    binaryPainter.finish(gc)
  }

  @StyleDsl
  class Style {
    var lineWidth: Double = 5.0
    var stroke: ColorProvider = Color.rgba(10, 10, 10, 0.5).asProvider()
    var shadow: ColorProviderNullable = null
    var areaFill: ColorProviderNullable = null
    var shadowOffset: Double = 4.0
  }
}
