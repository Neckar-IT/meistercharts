package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.BinaryValueRange
import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.ContentAreaLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.BinaryPainter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableColorNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity

/**
 * A simple hello world demo
 */
class BinaryPainterDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Binary Painter"

  //language=HTML
  override val description: String = "## Shows a binary painter"
  override val category: DemoCategory = DemoCategory.Painters

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
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
          layers.addLayer(ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { BinaryValueRange })) {
            titleProvider = { _, _ -> "Binary [Boolean]" }
            paintRange = AxisStyle.PaintRange.ContentArea
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

          configurableDouble("line width", painterLayer.style::lineWidth) {
            max = 20.0
            onChange {
              painterLayer.style.lineWidth = it
              markAsDirty()
            }
          }

          configurableColor("stroke", painterLayer.style::stroke) {}

          configurableColorNullable("shadow", painterLayer.style::shadow) {}

          configurableDouble("shadowOffset", painterLayer.style::shadowOffset) {
            max = 20.0
          }


          configurableColorNullable("areaFill", painterLayer.style::areaFill) {}
        }
      }
    }
  }
}

class MyBinaryPainterLayer(
  styleConfigurer: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfigurer)

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
    var stroke: Color = Color.rgba(10, 10, 10, 0.5)
    var shadow: Color? = null
    var areaFill: Color? = null
    var shadowOffset: Double = 4.0
  }
}
