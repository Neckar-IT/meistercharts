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

import com.meistercharts.model.Orientation
import com.meistercharts.algorithms.layers.ThresholdsLayer
import com.meistercharts.algorithms.layers.ThresholdsLayer.ThresholdValues
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.LineJoin
import com.meistercharts.canvas.LineSpacing
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.Importance
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.demo.section
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Insets
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.kotlin.lang.asProvider
import com.meistercharts.style.BoxStyle


class ThresholdsLayerDemoDescriptor : ChartingDemoDescriptor<Orientation> {
  override val name: String = "Thresholds layer"
  override val description: String = "How to visualize thresholds"
  override val category: DemoCategory = DemoCategory.Layers
  override val importance: Importance = Importance.Deprecated

  override val predefinedConfigurations: List<PredefinedConfiguration<Orientation>> = listOf(
    PredefinedConfiguration(Orientation.Vertical, "Vertical"),
    PredefinedConfiguration(Orientation.Horizontal, "Horizontal")
  )

  override fun createDemo(configuration: PredefinedConfiguration<Orientation>?): ChartingDemo {
    require(configuration != null) { "configuration must not be null" }

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val thresholds = object {
            @DomainRelative
            var minThreshold = 0.25

            @DomainRelative
            var maxThreshold = 0.75
          }

          val thresholdValues = object : DoublesProvider {
            override fun size(): Int = 2

            override fun valueAt(index: Int): Double {
              return when (index) {
                0 -> thresholds.minThreshold
                1 -> thresholds.maxThreshold
                else -> throw IndexOutOfBoundsException("$index")
              }
            }
          }

          val thresholdLabels = object : MultiProvider<ThresholdValues, List<String>> {
            override fun valueAt(index: Int): List<String> {
              //if vertical use two lines else one line
              return when (index) {
                0 -> {
                  if (configuration.payload == Orientation.Vertical) {
                    listOf("Minimum value", decimalFormat2digits.format(thresholds.minThreshold))
                  } else {
                    listOf("Minimum value ${decimalFormat2digits.format(thresholds.minThreshold)}")
                  }
                }

                1 -> {
                  if (configuration.payload == Orientation.Vertical) {
                    listOf("Maximum value", decimalFormat2digits.format(thresholds.maxThreshold))
                  } else {
                    listOf("Maximum value ${decimalFormat2digits.format(thresholds.maxThreshold)}")
                  }
                }

                else -> throw IndexOutOfBoundsException("$index")
              }
            }
          }

          val layer = ThresholdsLayer(ThresholdsLayer.Data(thresholdValues, thresholdLabels)) {
            orientation = configuration.payload
          }

          if (configuration.payload == Orientation.Horizontal) {
            //improve style for horizontal orientation
            layer.style.passpartout = Insets.onlyTop(40.0)
            layer.style.anchorPointProvider = DirectionBasedBasePointProvider(Direction.TopCenter)
            layer.style.anchorDirection = Direction.BottomCenter
            layer.style.anchorGapHorizontal = 5.0
            layer.style.anchorGapVertical = 5.0
            layer.style.horizontalAlignment = HorizontalAlignment.Center
          }

          var lineStyle = layer.style.lineStyles.valueAt(0)

          layer.style.lineStyles = MultiProvider { lineStyle }

          layers.addLayer(layer)

          configurableInsetsSeparate("Passpartout") {
            value = layer.style.passpartout
            min = 0.0
            max = 300.0
            onChange {
              layer.style.passpartout = it
              markAsDirty()
            }
          }

          configurableBoolean("High contrast") {
            value = false
            val oldTextColor = layer.style.textColor
            val oldLineColor = lineStyle.color
            val oldBoxStyle = layer.style.boxStyle()
            onChange {
              if (it) {
                layer.style.textColor = Color.magenta
                layer.style.boxStyle = BoxStyle(Color.white, Color.red, 2.0).asProvider()
                lineStyle = lineStyle.copy(color = Color.magenta)
              } else {
                layer.style.textColor = oldTextColor
                layer.style.boxStyle = oldBoxStyle.asProvider()
                lineStyle = lineStyle.copy(color = oldLineColor)
              }
              markAsDirty()
            }
          }

          section("Threshold labels")
          configurableBoolean("Show", layer.style::showThresholdLabel)
          configurableColorPicker("Color", layer.style::textColor)
          configurableFont("Font", layer.style::font)
          configurableList("Anchor location", (layer.style.anchorPointProvider as DirectionBasedBasePointProvider).direction, Direction.values().toList()) {
            onChange {
              layer.style.anchorPointProvider = DirectionBasedBasePointProvider(it)
              markAsDirty()
            }
          }
          configurableListWithProperty("Anchor direction", layer.style::anchorDirection, Direction.values().toList())
          configurableDouble("Anchor gap", layer.style::anchorGapHorizontal) {
            min = 0.0
            max = 100.0
          }
          configurableDouble("Anchor gap", layer.style::anchorGapVertical) {
            min = 0.0
            max = 100.0
          }
          configurableListWithProperty("H-Alignment", layer.style::horizontalAlignment, HorizontalAlignment.values().toList())
          configurableDouble("Line spacing", layer.style.lineSpacing.percentage) {
            min = 0.0
            max = 10.0
            onChange {
              layer.style.lineSpacing = LineSpacing(it)
              markAsDirty()
            }
          }

          section("Line style")

          configurableColorPicker("Color", lineStyle.color) {
            onChange {
              lineStyle = lineStyle.copy(color = it)
              markAsDirty()
            }
          }

          configurableDouble("Width", lineStyle.lineWidth) {
            min = 0.0
            max = 100.0
            onChange {
              lineStyle = lineStyle.copy(lineWidth = it)
              markAsDirty()
            }
          }

          configurableList("Dashes", lineStyle.dashes, Dashes.predefined) {
            onChange {
              lineStyle = lineStyle.copy(dashes = it)
              markAsDirty()
            }
          }

          configurableList("Line join", lineStyle.lineJoin, LineJoin.values().toList()) {
            onChange {
              lineStyle = lineStyle.copy(lineJoin = it)
              markAsDirty()
            }
          }

          section("Thresholds")

          configurableDouble("Min threshold", thresholds::minThreshold) {
            min = 0.0
            max = 1.0
          }

          configurableDouble("Max threshold", thresholds::maxThreshold) {
            min = 0.0
            max = 1.0
          }
        }
      }
    }
  }
}
