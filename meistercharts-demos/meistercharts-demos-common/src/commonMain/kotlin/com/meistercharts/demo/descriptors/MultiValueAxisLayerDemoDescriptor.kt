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
import com.meistercharts.range.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.axis.MultiValueAxisLayer
import com.meistercharts.algorithms.layers.MultipleLayersDelegatingLayer
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.axis.ValueAxesProvider
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.AxisTitleLocation
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.strokeStyle
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.charts.support.ValueAxisSupport
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Insets
import it.neckar.open.provider.SizedProvider

/**
 * Demo for [MultiValueAxisLayer]
 */
class MultiValueAxisLayerDemoDescriptor : MeisterchartsDemoDescriptor<MultiValueAxisLayerDemoDescriptor.Model> {
  override val uuid: Uuid = Uuid.parse("ddb10ba6-e299-4e72-8108-194e1c8cb1bb")
  override val name: String = "Multiple value axes"
  override val description: String = "How to lay out multiple value axes"
  override val category: DemoCategory = DemoCategory.Axis

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<Model>> = listOf(
    PredefinedConfiguration(Uuid.parse("524af6d7-bb2f-47b7-b7ce-d34c1bb54b05"), createDefaultModel(), "Default"),
    PredefinedConfiguration(Uuid.parse("77031785-83ff-4fe7-b698-54360dc8975c"), createTitleOnTop(), "Title on top"),
    PredefinedConfiguration(Uuid.parse("3470eccf-d0a0-4118-b184-f5bb0803be32"), createTitleOnTopPaintRangeContentArea(), "Paint Range: Content Area"),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<Model>?): MeisterchartsDemo {
    requireNotNull(configuration) { "MultiValueAxisLayerDemoDescriptor.prepareDemo requires a predefined configuration" }

    return MeisterchartsDemo(this) {
      val model = configuration.payload
      val multiValueAxisLayer = model.createMultiValueAxisLayer()
      val topTitleLayer = model.createTopTitleLayer()

      val passpartoutLayer = PasspartoutLayer {
        color = Color.lightgoldenrodyellow
        insets = { Insets.onlyLeft(multiValueAxisLayer.paintingVariables().totalWidth) }
      }

      meistercharts {
        val contentViewportGestalt = ContentViewportGestalt(model.initialContentViewportMargin)
        contentViewportGestalt.configure(this@meistercharts)

        configure {
          layers.addClearBackground()

          layers.addLayer(passpartoutLayer)
          layers.addLayer(multiValueAxisLayer)
          if (topTitleLayer != null) {
            layers.addLayer(topTitleLayer)
          }

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              @Window val lineX = multiValueAxisLayer.configuration.valueAxesMaxWidthPercentage * paintingContext.width
              paintingContext.gc.strokeStyle(Color.red)
              paintingContext.gc.strokeLine(lineX, 0.0, lineX, paintingContext.height)
            }
          })

          configurable("Background", passpartoutLayer.configuration::color) {
          }

          configurable("Axes count", model::visibleAxisCount) {
            max = model.axisCount
          }

          configurable("Max relative width", multiValueAxisLayer.configuration::valueAxesMaxWidthPercentage) {
            min = 0.0
            max = 1.0
          }

          configurable("Gap between axes", multiValueAxisLayer.configuration::valueAxesGap) {
            min = -10.0
            max = 200.0
          }

          configurableSeparate("Content Viewport", contentViewportGestalt::contentViewportMargin)
        }
      }
    }
  }

  interface Model {
    var visibleAxisCount: Int
    val axisCount: Int

    val initialContentViewportMargin: @Zoomed Insets

    fun createMultiValueAxisLayer(): MultiValueAxisLayer

    fun createTopTitleLayer(): MultipleLayersDelegatingLayer<AxisTopTopTitleLayer>? {
      return null
    }
  }

  companion object {
    fun createDefaultModel(): Model {
      return object : Model {
        override val initialContentViewportMargin: Insets = Insets.all15

        val valueAxisSupport = ValueAxisSupport<Int>(
          valueRangeProvider = { i ->
            ValueRange.linear(0.0, 100.0 + i * 10)
          },
        ) {
          this.valueAxisConfiguration = { index, axis, valueAxisTitleLocation ->
            titleProvider = { _, _ -> "The ${index + 1}th value axis" }
            size = 120.0
          }
        }

        override val axisCount: Int
          get() {
            return 4
          }

        override var visibleAxisCount: Int = 3

        override fun createMultiValueAxisLayer(): MultiValueAxisLayer {
          val valueAxesProvider: ValueAxesProvider = object : ValueAxesProvider {
            override fun size(): Int {
              return visibleAxisCount
            }

            override fun valueAt(index: Int): ValueAxisLayer {
              return valueAxisSupport.getAxisLayer(index)
            }
          }

          val multiValueAxisLayer = MultiValueAxisLayer(valueAxesProvider) {
            valueAxesGap = 25.0
          }

          return multiValueAxisLayer
        }
      }
    }

    fun createTitleOnTop(): Model {
      return object : Model {
        override val initialContentViewportMargin: Insets = Insets.of(40.0, 10.0, 10.0, 10.0)

        val valueAxisSupport = ValueAxisSupport<Int>(
          valueRangeProvider = { i ->
            ValueRange.linear(0.0, 100.0 + i * 10)
          },
        ) {
          this.valueAxisConfiguration = { index, axis, valueAxisTitleLocation ->
            titleProvider = { _, _ -> "The ${index + 1}th value axis" }
          }
        }.also {
          it.preferredAxisTitleLocation = AxisTitleLocation.AtTop
        }

        override val axisCount: Int
          get() {
            return 7
          }

        override var visibleAxisCount: Int = 3

        override fun createMultiValueAxisLayer(): MultiValueAxisLayer {
          return MultiValueAxisLayer(
            valueAxesProvider = object : ValueAxesProvider {
              override fun size(): Int {
                return visibleAxisCount
              }

              override fun valueAt(index: Int): ValueAxisLayer {
                return valueAxisSupport.getAxisLayer(index)
              }
            },
          ) {
            valueAxesGap = 25.0
          }
        }

        override fun createTopTitleLayer(): MultipleLayersDelegatingLayer<AxisTopTopTitleLayer> {
          return MultipleLayersDelegatingLayer(object : SizedProvider<AxisTopTopTitleLayer> {
            override fun size(): Int {
              return visibleAxisCount
            }

            override fun valueAt(index: Int): AxisTopTopTitleLayer {
              return valueAxisSupport.getTopTitleLayer(index)
            }
          })
        }
      }
    }

    fun createTitleOnTopPaintRangeContentArea(): Model {
      return object : Model {
        override val initialContentViewportMargin: Insets = Insets.of(40.0, 10.0, 10.0, 10.0)

        val valueAxisSupport = ValueAxisSupport<Int>(
          valueRangeProvider = { i ->
            ValueRange.linear(0.0, 100.0 + i * 10)
          },
        ) {
          this.valueAxisConfiguration = { index, axis, valueAxisTitleLocation ->
            titleProvider = { _, _ -> "The ${index + 1}th value axis" }
            paintRange = AxisConfiguration.PaintRange.ContentArea
          }
        }.also {
          it.preferredAxisTitleLocation = AxisTitleLocation.AtTop
        }

        override val axisCount: Int
          get() {
            return 7
          }

        override var visibleAxisCount: Int = 3

        override fun createMultiValueAxisLayer(): MultiValueAxisLayer {
          return MultiValueAxisLayer(
            valueAxesProvider = object : ValueAxesProvider {
              override fun size(): Int {
                return visibleAxisCount
              }

              override fun valueAt(index: Int): ValueAxisLayer {
                return valueAxisSupport.getAxisLayer(index)
              }
            },
          ) {
            valueAxesGap = 25.0
          }
        }

        override fun createTopTitleLayer(): MultipleLayersDelegatingLayer<AxisTopTopTitleLayer>? {
          return MultipleLayersDelegatingLayer(object : SizedProvider<AxisTopTopTitleLayer> {
            override fun size(): Int {
              return visibleAxisCount
            }

            override fun valueAt(index: Int): AxisTopTopTitleLayer {
              return valueAxisSupport.getTopTitleLayer(index)
            }
          })
        }
      }
    }
  }
}
