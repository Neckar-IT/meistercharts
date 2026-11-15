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
import it.neckar.geometry.Orientation
import com.meistercharts.algorithms.layers.ThresholdsLayer
import com.meistercharts.algorithms.layers.ThresholdsLayer.ThresholdValues
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.color.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.LineJoin
import com.meistercharts.canvas.text.LineSpacing
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.demo.createEnumConfigs
import com.meistercharts.demo.section
import it.neckar.geometry.Direction
import com.meistercharts.geometry.DirectionBasedBasePointProvider
import it.neckar.geometry.HorizontalAlignment
import com.meistercharts.model.Insets
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.kotlin.lang.asProvider
import com.meistercharts.style.BoxStyle


class ThresholdsLayerDemoDescriptor : MeisterchartsDemoDescriptor<Orientation> {
  override val uuid: Uuid = Uuid.parse("a6ebcf45-4f7c-450a-8940-c77d087c4ed1")
  override val name: String = "Thresholds layer"
  override val description: String = "How to visualize thresholds"
  override val category: DemoCategory = DemoCategory.Layers

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<Orientation>> = createEnumConfigs()

  override fun prepareDemo(configuration: PredefinedConfiguration<Orientation>?): MeisterchartsDemo {
    require(configuration != null) { "configuration must not be null" }

    return MeisterchartsDemo(this) {
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

          val layer = ThresholdsLayer(thresholdValues, thresholdLabels) {
            orientation = configuration.payload
          }

          if (configuration.payload == Orientation.Horizontal) {
            //improve style for horizontal orientation
            layer.configuration.passpartout = Insets.onlyTop(40.0)
            layer.configuration.anchorPointProvider = DirectionBasedBasePointProvider(Direction.TopCenter)
            layer.configuration.anchorDirection = Direction.BottomCenter
            layer.configuration.anchorGapHorizontal = 5.0
            layer.configuration.anchorGapVertical = 5.0
            layer.configuration.horizontalAlignment = HorizontalAlignment.Center
          }

          var lineStyle = layer.configuration.lineStyles.valueAt(0)

          layer.configuration.lineStyles = MultiProvider { lineStyle }

          layers.addLayer(layer)

          configurableInsetsSeparate("Passpartout") {
            value = layer.configuration.passpartout
            min = 0.0
            max = 300.0
            onChange {
              layer.configuration.passpartout = it
              markAsDirty()
            }
          }

          configurableBoolean("High contrast") {
            value = false
            val oldTextColor = layer.configuration.textColor
            val oldLineColor = lineStyle.color
            val oldBoxStyle = layer.configuration.boxStyle()
            onChange {
              if (it) {
                layer.configuration.textColor = Color.magenta
                layer.configuration.boxStyle = BoxStyle(Color.white, Color.red, 2.0).asProvider()
                lineStyle = lineStyle.copy(color = Color.magenta)
              } else {
                layer.configuration.textColor = oldTextColor
                layer.configuration.boxStyle = oldBoxStyle.asProvider()
                lineStyle = lineStyle.copy(color = oldLineColor)
              }
              markAsDirty()
            }
          }

          section("Threshold labels")
          configurable("Show", layer.configuration::showThresholdLabel)
          configurable("Color", layer.configuration::textColor)
          configurable("Font", layer.configuration::font)
          configurableList("Anchor location", (layer.configuration.anchorPointProvider as DirectionBasedBasePointProvider).direction(), Direction.entries) {
            onChange {
              layer.configuration.anchorPointProvider = DirectionBasedBasePointProvider(it)
              markAsDirty()
            }
          }
          configurableListWithProperty("Anchor direction", layer.configuration::anchorDirection, Direction.entries)
          configurable("Anchor gap", layer.configuration::anchorGapHorizontal) {
            min = 0.0
            max = 100.0
          }
          configurable("Anchor gap", layer.configuration::anchorGapVertical) {
            min = 0.0
            max = 100.0
          }
          configurableListWithProperty("H-Alignment", layer.configuration::horizontalAlignment, HorizontalAlignment.entries)
          configurableDouble("Line spacing", layer.configuration.lineSpacing.percentage) {
            min = 0.0
            max = 10.0
            onChange {
              layer.configuration.lineSpacing = LineSpacing(it)
              markAsDirty()
            }
          }

          section("Line style")

          configurableColorPicker("Color", lineStyle.color()) {
            onChange {
              lineStyle = lineStyle.copy(color = it.asProvider())
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

          configurableList("Line join", lineStyle.lineJoin, LineJoin.entries) {
            onChange {
              lineStyle = lineStyle.copy(lineJoin = it)
              markAsDirty()
            }
          }

          section("Thresholds")

          configurable("Min threshold", thresholds::minThreshold) {
            min = 0.0
            max = 1.0
          }

          configurable("Max threshold", thresholds::maxThreshold) {
            min = 0.0
            max = 1.0
          }
        }
      }
    }
  }
}
