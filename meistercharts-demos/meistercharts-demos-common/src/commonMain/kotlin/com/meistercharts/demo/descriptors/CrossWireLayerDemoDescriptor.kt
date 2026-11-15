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
import com.meistercharts.demo.configurableSeparate
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.algorithms.layers.crosswire.LabelPlacementStrategy
import com.meistercharts.algorithms.layers.debug.addPaintInfoDebug
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.demo.section
import com.meistercharts.model.Insets
import com.meistercharts.resize.KeepLocation
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Palette.getChartColor
import com.meistercharts.time.TimeRange
import it.neckar.geometry.Distance
import it.neckar.geometry.VerticalAlignment
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.provider.MultiProvider

/**
 * Demos that visualizes the functionality of the [CrossWireLayer]
 */
open class CrossWireLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("f5aceadd-0711-40f5-ae3f-76e7e194dd52")
  override val name: String = "Cross Wire Layer"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "A cross wire with labels"
  override val category: DemoCategory = DemoCategory.Layers

  protected open fun configureDemo(meisterchartsDemo: MeisterchartsDemo) {
    with(meisterchartsDemo) {
      meistercharts {

        val configuration = object {
          /**
           * the cross wire location
           */
          var crossWireLocationX: @WindowRelative Double = 0.75
        }

        val contentAreaTimeRange: TimeRange = TimeRange.oneHourUntilNow()

        val crossWireValueLabelsProvider = object : CrossWireLayer.ValueLabelsProvider {
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

        val crossWireLayer = CrossWireLayer(crossWireValueLabelsProvider) {
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



        val contentViewportGestalt = ContentViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this)

        configure {
          chartSupport.windowResizeBehavior = KeepLocation({ configuration.crossWireLocationX }, { 0.5 })

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

          crossWireLayer.configuration.apply {
            valueLabelsStart = { minMaxDebugLayer.labelStart }
            valueLabelsEnd = { minMaxDebugLayer.labelEnd }
          }

          layers.addLayer(minMaxDebugLayer)
          layers.addLayer(crossWireLayer)
          layers.addPaintInfoDebug()

          section("Data")
          configurable("Value (y)", crossWireValueLabelsProvider::location) {
            min = 0.0
            max = 1000.0
          }
          configurable("Label Delta  (y)", crossWireValueLabelsProvider::delta) {
            min = 0.0
            max = 100.0
          }

          section("Wire")

          configurable("Show wire", crossWireLayer.configuration::showCrossWireLine)

          configurable("Wire width", crossWireLayer.configuration::wireWidth) {
            min = 0.0
            max = 20.0
          }

          configurableColorProvider("Wire color", crossWireLayer.configuration::wireColor)


          section("Labels")

          configurable("Labels Start Y", minMaxDebugLayer::labelStart) {
            max = 500.0
          }

          configurable("Labels End Y", minMaxDebugLayer::labelEnd) {
            max = 1500.0
          }

          configurable("Show value labels", crossWireLayer.configuration::showValueLabels)

          configurable("Show position label", crossWireLayer.configuration::showCurrentLocationLabel)

          configurable("Show lines to labels", crossWireLayer.configuration::showLineToValueBox)

          configurable("Location (x)", configuration::crossWireLocationX) {
            min = 0.0
            max = 1.0
          }

          configurableList(
            "Label-placement strategy", crossWireLayer.configuration.valueLabelPlacementStrategy, listOf(
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
              crossWireLayer.configuration.valueLabelPlacementStrategy = it
              markAsDirty()
            }
          }
          configurableDouble("Min width for labels on right side", 150.0) {
            min = 0.0
            max = 500.0

            onChange {
              crossWireLayer.configuration.valueLabelPlacementStrategy = LabelPlacementStrategy.preferOnRightSide { it }
              markAsDirty()
            }
          }

          section("Current Location Label")
          configurable("Font", crossWireLayer.configuration::currentLocationLabelFont)
          configurableColorProvider("Text Color", crossWireLayer.configuration::currentLocationLabelTextColor)

          configurable("Anchor Direction", crossWireLayer.configuration::currentLocationLabelAnchorDirection)

          configurableEnum("Anchor Vertical Alignment", VerticalAlignment.Top) {
            onChange {
              crossWireLayer.configuration.currentLocationLabelAnchorPoint = { paintingContext ->
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

          configurableListWithProperty("Box Style", crossWireLayer.configuration::currentLocationLabelBoxStyle, listOf(BoxStyle.black, BoxStyle.gray, BoxStyle.none)) {
            converter { "${it.fill} | ${it.borderColor}" }
          }

          configurable("Value-label font", crossWireLayer.configuration::valueLabelFont) {}


          configurableSeparate("Content Viewport Margin", contentViewportGestalt::contentViewportMargin)
        }
      }
    }
  }

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      configureDemo(this)
    }
  }

}
