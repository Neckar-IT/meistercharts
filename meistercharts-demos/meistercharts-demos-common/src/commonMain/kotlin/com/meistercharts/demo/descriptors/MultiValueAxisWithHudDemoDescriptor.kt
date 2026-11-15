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
import com.meistercharts.range.ValueRange
import com.meistercharts.algorithms.layers.DirectionalLinesLayer
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.layers.MultipleLayersDelegatingLayer
import com.meistercharts.algorithms.layers.axis.MultiValueAxisLayer
import com.meistercharts.algorithms.layers.axis.ValueAxesProvider
import com.meistercharts.algorithms.layers.axis.HudLabelsProvider
import com.meistercharts.algorithms.layers.axis.ValueAxisHudLayer
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.axis.hudLayer
import com.meistercharts.color.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.model.BorderRadius
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontWeight
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.collections.getModulo
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.kotlin.lang.asProvider
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Palette
import it.neckar.geometry.Direction

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class MultiValueAxisWithHudDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("eea3dbc8-193d-4a51-94c1-df22fa6ed0c8")
  override val name: String = "Value axes (multiple axis) - with HUD"

  override val category: DemoCategory = DemoCategory.Axis

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

      meistercharts {

        val maxCount = 10

        configure {
          val config = object {
            var layersCount = 3

            var thresholdValues: @Domain DoubleArray = DoubleArray(maxCount) {
              it * 10.0
            }
          }

          val valueAxisLayers = List(maxCount) { index ->
            ValueAxisLayer("Value $index", ValueRange.default) {
              lineColor = Palette.getChartColor(index)
              tickLabelColor = lineColor
              titleColor = lineColor
            }
          }

          val multiValueAxisLayer = MultiValueAxisLayer(object : ValueAxesProvider {
            override fun valueAt(index: Int): ValueAxisLayer {
              return valueAxisLayers[index]
            }

            override fun size(): Int {
              return config.layersCount
            }
          })

          val hudLayers = valueAxisLayers.mapIndexed { valueLayerIndex, valueAxisLayer ->
            valueAxisLayer.hudLayer(object : DoublesProvider {
              override fun size(): Int {
                return 1
              }

              override fun valueAt(index: Int): Double {
                return config.thresholdValues[valueLayerIndex]
              }
            }).apply {
              this.configuration.boxStyles = MultiProvider.always(
                BoxStyle(
                  fill = Color.white,
                  borderColor = Palette.getChartColor(valueLayerIndex),
                  radii = BorderRadius.all2,
                  padding = Insets.of(5.0, 8.0)
                )
              )
              this.configuration.textColors = MultiProvider.alwaysProvider(Palette.getChartColor(valueLayerIndex))
              this.configuration.textFonts = MultiProvider.always(FontDescriptorFragment(weight = FontWeight.Bold))
            }
          }

          val thresholdLineLayers = valueAxisLayers.mapIndexed { valueLayerIndex, valueAxisLayer ->
            val hudLayer = hudLayers[valueLayerIndex]
            DirectionalLinesLayer.createForValueAxisAndHud(valueAxisLayer, hudLayer).also {
              it.configuration.lineEndsAtMode = DirectionalLinesLayer.LineEndsAtMode.WithinContentViewport.asProvider()
            }
          }

          layers.addClearBackground()
          layers.addLayer(multiValueAxisLayer)

          layers.addLayers(hudLayers, thresholdLineLayers) {
            multiValueAxisLayer.paintingVariables().visibleAxisCount
          }

          configurable("Axis count", config::layersCount) {
            max = valueAxisLayers.size
          }

          configurableEnum("Axis tick orientation", valueAxisLayers.first().configuration.tickOrientation, kotlin.enums.enumEntries<Vicinity>().toList()) {
            onChange { vicinity ->
              valueAxisLayers.fastForEach { it.configuration.tickOrientation = vicinity }
              markAsDirty()
            }
          }

          configurableEnum("Hud anchor direction", hudLayers.first().configuration.anchorDirections.valueAt(0), kotlin.enums.enumEntries<Direction>().toList()) {
            onChange { direction ->
              hudLayers.fastForEach { hudLayer ->
                hudLayer.configuration.anchorDirections = MultiProvider.always(direction)
              }
              markAsDirty()
            }
          }

          configurableList("Label", "", listOf("min.", "Maximum", "A very long laaaaaabbbbeeeeellll")) {
            onChange { label ->
              hudLayers.fastForEachIndexed { layerIndex, hudLayer ->
                hudLayer.configuration.labels = HudLabelsProvider { _, _ ->
                  if (label.isEmpty()) {
                    listOf(config.thresholdValues.getModulo(layerIndex).toString())
                  } else {
                    listOf(label, config.thresholdValues.getModulo(layerIndex).toString())
                  }
                }
              }
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

private fun Layers.addLayers(hudLayers: List<ValueAxisHudLayer>, thresholdLineLayers: List<DirectionalLinesLayer>, visibleCountProvider: () -> Int) {
  val hudLayerDelegateIndex = addLayer(
    MultipleLayersDelegatingLayer(object : SizedProvider<ValueAxisHudLayer> {
      override fun size(): Int {
        return visibleCountProvider()
      }

      override fun valueAt(index: Int): ValueAxisHudLayer {
        return hudLayers[index]
      }
    })
  )

  addLayerAt(
    MultipleLayersDelegatingLayer(object : SizedProvider<DirectionalLinesLayer> {
      override fun size(): Int {
        return visibleCountProvider()
      }

      override fun valueAt(index: Int): DirectionalLinesLayer {
        return thresholdLineLayers[index]
      }
    }),
    hudLayerDelegateIndex, hudLayerDelegateIndex + 1
  )
}
