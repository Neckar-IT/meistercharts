package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.DirectionalLinesLayer
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.layers.MultipleLayersDelegatingLayer
import com.meistercharts.algorithms.layers.MultiValueAxisLayer
import com.meistercharts.algorithms.layers.ValueAxesProvider
import com.meistercharts.algorithms.layers.HudLabelsProvider
import com.meistercharts.algorithms.layers.ValueAxisHudLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.hudLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.BorderRadius
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontWeight
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Insets
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.collections.getModulo
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.kotlin.lang.asProvider
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Palette

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class MultiValueAxisWithHudDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value axes (multiple axis) - with HUD"

  override val category: DemoCategory = DemoCategory.Axis

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

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
              lineColor = Palette.getChartColor(index).asProvider()
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
              this.configuration.textColors = MultiProvider.always(Palette.getChartColor(valueLayerIndex))
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

          configurableInt("Axis count", config::layersCount) {
            max = valueAxisLayers.size
          }

          configurableEnum("Axis tick orientation", valueAxisLayers.first().style.tickOrientation, enumValues()) {
            onChange { vicinity ->
              valueAxisLayers.fastForEach { it.style.tickOrientation = vicinity }
              markAsDirty()
            }
          }

          configurableEnum("Hud anchor direction", hudLayers.first().configuration.anchorDirections.valueAt(0), enumValues()) {
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
