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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.KeepLocation
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.algorithms.layers.crosswire.LabelPlacementStrategy
import com.meistercharts.algorithms.layers.debug.addPaintInfoDebug
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.demo.section
import com.meistercharts.model.Distance
import com.meistercharts.model.Insets
import com.meistercharts.model.VerticalAlignment
import it.neckar.open.provider.MultiProvider
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Palette.getChartColor

/**
 * Demos that visualizes the functionality of the [CrossWireLayer]
 */
open class CrossWireLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Cross Wire Layer"

  //language=HTML
  override val description: String = """<h4>Cross wire with labels</h4>
    | third label has empty label string
  """.trimMargin()
  override val category: DemoCategory = DemoCategory.Layers

  val contentAreaTimeRange: TimeRange = TimeRange.oneHourUntilNow()

  private val crossWireValueLabelsProvider = object : CrossWireLayer.ValueLabelsProvider {
    var location: @Window Double = 250.0
    var delta = 15.0

    override fun size(): Int {
      return 10
    }

    override fun layout(wireLocation: Double, paintingContext: LayerPaintingContext) {
      //Do nothing!
    }

    override fun locationAt(index: Int): @Window Double {
      return location + index * delta
    }

    override fun labelAt(index: Int, textService: TextService, i18nConfiguration: I18nConfiguration): String {
      if (index == 2) {
        return ""
      }

      return "Label @ ${decimalFormat.format(locationAt(index))}"
    }
  }

  val crossWireLayer: CrossWireLayer = CrossWireLayer(CrossWireLayer.Data(crossWireValueLabelsProvider) { _, crossWireLocation -> "Position @ $crossWireLocation" }) {
    showCurrentLocationLabel = true
    showValueLabels = true

    valueLabelBoxStyle = MultiProvider { index ->
      if (index == 2) {
        BoxStyle(fill = getChartColor(index), borderColor = Color.red, padding = Insets.of(10.0))
      } else {
        BoxStyle(fill = getChartColor(index), borderColor = Color.silver)
      }
    }
  }

  /**
   * the cross wire location
   */
  var crossWireLocationX: @WindowRelative Double = 0.75

  protected open fun configureDemo(chartingDemo: ChartingDemo) {
    with(chartingDemo) {
      meistercharts {

        val contentViewportGestalt = ContentViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this)

        configure {
          chartSupport.windowResizeBehavior = KeepLocation({ crossWireLocationX }, { 0.5 })

          layers.addClearBackground()

          val minMaxDebugLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var labelStart: @Window Double = 10.0
            var labelEnd: @Window Double = 500.0

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartCalculator = paintingContext.chartCalculator

              gc.strokeLine(0.0, labelStart, gc.width, labelStart)
              gc.strokeLine(0.0, labelEnd, gc.width, labelEnd)
            }
          }

          crossWireLayer.style.apply {
            valueLabelsStart = { minMaxDebugLayer.labelStart }
            valueLabelsEnd = { minMaxDebugLayer.labelEnd }
          }

          layers.addLayer(minMaxDebugLayer)
          layers.addLayer(crossWireLayer)
          layers.addPaintInfoDebug()

          section("Data")
          configurableDouble("Value (y)", crossWireValueLabelsProvider::location) {
            min = 0.0
            max = 1000.0
          }
          configurableDouble("Label Delta  (y)", crossWireValueLabelsProvider::delta) {
            min = 0.0
            max = 100.0
          }

          section("Wire")

          configurableBoolean("Show wire", crossWireLayer.style::showCrossWireLine)

          configurableDouble("Wire width", crossWireLayer.style::wireWidth) {
            min = 0.0
            max = 20.0
          }

          configurableColor("Wire color", crossWireLayer.style::wireColor)


          section("Labels")

          configurableDouble("Labels Start Y", minMaxDebugLayer::labelStart) {
            max = 500.0
          }

          configurableDouble("Labels End Y", minMaxDebugLayer::labelEnd) {
            max = 1500.0
          }

          configurableBoolean("Show value labels", crossWireLayer.style::showValueLabels)

          configurableBoolean("Show position label", crossWireLayer.style::showCurrentLocationLabel)

          configurableBoolean("Show lines to labels", crossWireLayer.style::showLineToValueBox)

          configurableDouble("Location (x)", ::crossWireLocationX) {
            min = 0.0
            max = 1.0
          }

          configurableList(
            "Label-placement strategy", crossWireLayer.style.valueLabelPlacementStrategy, listOf(
              LabelPlacementStrategy.AlwaysOnRightSide,
              LabelPlacementStrategy.AlwaysOnLeftSide,
              LabelPlacementStrategy.preferOnRightSide { 150.0 },
            )
          ) {
            converter {
              when (it) {
                LabelPlacementStrategy.AlwaysOnRightSide -> "always on right"
                LabelPlacementStrategy.AlwaysOnLeftSide -> "always on left"
                else -> "prefer on right"
              }
            }

            onChange {
              crossWireLayer.style.valueLabelPlacementStrategy = it
              markAsDirty()
            }
          }
          configurableDouble("Min width for labels on right side", 150.0) {
            min = 0.0
            max = 500.0

            onChange {
              crossWireLayer.style.valueLabelPlacementStrategy = LabelPlacementStrategy.preferOnRightSide { it }
              markAsDirty()
            }
          }

          section("Current Location Label")
          configurableFont("Font", crossWireLayer.style::currentLocationLabelFont)
          configurableColor("Text Color", crossWireLayer.style::currentLocationLabelTextColor)

          configurableEnum("Anchor Direction", crossWireLayer.style::currentLocationLabelAnchorDirection)

          configurableEnum("Anchor Vertical Alignment", VerticalAlignment.Top) {
            onChange {
              crossWireLayer.style.currentLocationLabelAnchorPoint = { paintingContext ->
                when (it) {
                  VerticalAlignment.Top -> Distance.none
                  VerticalAlignment.Center -> Distance.of(0.0, paintingContext.height / 2.0)
                  VerticalAlignment.Baseline -> Distance.of(0.0, paintingContext.height / 2.0)
                  VerticalAlignment.Bottom -> Distance.of(0.0, paintingContext.height)
                }
              }
              this@with.markAsDirty()
            }
          }

          configurableListWithProperty("Box Style", crossWireLayer.style::currentLocationLabelBoxStyle, listOf(BoxStyle.black, BoxStyle.gray, BoxStyle.none)) {
            converter { "${it.fill} | ${it.borderColor}" }
          }

          configurableFont("Value-label font", crossWireLayer.style::valueLabelFont) {}


          configurableInsetsSeparate("Content Viewport Margin", contentViewportGestalt::contentViewportMargin)
        }
      }
    }
  }

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      configureDemo(this)
    }
  }

}
