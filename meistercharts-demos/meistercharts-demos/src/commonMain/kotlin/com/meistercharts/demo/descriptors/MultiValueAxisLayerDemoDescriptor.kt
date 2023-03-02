package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.MultiValueAxisLayer
import com.meistercharts.algorithms.layers.MultipleLayersDelegatingLayer
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.ValueAxesProvider
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.AxisTitleLocation
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.charts.support.ValueAxisSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Insets
import it.neckar.open.provider.SizedProvider

/**
 * Demo for [MultiValueAxisLayer]
 */
class MultiValueAxisLayerDemoDescriptor : ChartingDemoDescriptor<MultiValueAxisLayerDemoDescriptor.Model> {
  override val name: String = "Multiple value axes"
  override val description: String = "## How to layout multiple values axes"
  override val category: DemoCategory = DemoCategory.Axis

  override val predefinedConfigurations: List<PredefinedConfiguration<Model>> = listOf(
    PredefinedConfiguration(createDefaultModel(), "Default"),
    PredefinedConfiguration(createTitleOnTop(), "Title on top"),
    PredefinedConfiguration(createTitleOnTopPaintRangeContentArea(), "Paint Range: Content Area"),
  )

  override fun createDemo(configuration: PredefinedConfiguration<Model>?): ChartingDemo {
    requireNotNull(configuration)

    return ChartingDemo {
      val model = configuration.payload
      val multiValueAxisLayer = model.createMultiValueAxisLayer()
      val topTitleLayer = model.createTopTitleLayer()

      val passpartoutLayer = PasspartoutLayer {
        color = { Color.lightgoldenrodyellow }
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

          configurableColorPickerProvider("Background", passpartoutLayer.style::color) {
          }

          configurableInt("Axes count", model::visibleAxisCount) {
            max = model.axisCount
          }

          configurableDouble("Max relative width", multiValueAxisLayer.configuration::valueAxesMaxWidthPercentage) {
            min = 0.0
            max = 1.0
          }

          configurableDouble("Gap between axes", multiValueAxisLayer.configuration::valueAxesGap) {
            min = -10.0
            max = 200.0
          }

          configurableInsetsSeparate("Content Viewport", contentViewportGestalt::contentViewportMargin)
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
            paintRange = AxisStyle.PaintRange.ContentArea
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
