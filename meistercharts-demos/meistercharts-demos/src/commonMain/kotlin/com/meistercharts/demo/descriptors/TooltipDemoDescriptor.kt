package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addTooltipLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.TooltipContent
import com.meistercharts.canvas.tooltipSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Rectangle
import com.meistercharts.events.gesture.MouseMovementSupport
import it.neckar.open.observable.ObservableObject

class TooltipDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Tooltip"
  override val description: String = "## How to create a tooltip"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(TooltipDemoLayer())
          layers.addTooltipLayer()
        }
      }
    }
  }
}

/**
 * Sets the tooltip over a few rectangles
 */
class TooltipDemoLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  private val greenRect = Rectangle(10.0, 10.0, 100.0, 100.0)
  private val blueRect = Rectangle(110.0, 110.0, 100.0, 100.0)
  private val redRect = Rectangle(210.0, 210.0, 100.0, 100.0)

  override fun initialize(paintingContext: LayerPaintingContext) {
    super.initialize(paintingContext)

    val chartSupport = paintingContext.chartSupport

    val tooltipProperty = chartSupport.tooltipSupport.tooltipProperty(this)
    val mouseMovementSupport = MouseMovementSupport(chartSupport.mouseEvents.mousePositionProperty)

    mouseMovementSupport.mouseOver(ObservableObject(greenRect)) {
      tooltipProperty.value = if (it) TooltipContent("Green!") else null
    }

    mouseMovementSupport.mouseOver(ObservableObject(blueRect)) {
      tooltipProperty.value = if (it) TooltipContent("Blue!") else null
    }

    mouseMovementSupport.mouseOver(ObservableObject(redRect)) {
      tooltipProperty.value = if (it) TooltipContent("Red!") else null
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.fill(Color.green)
    gc.fillRect(greenRect)

    gc.fill(Color.blue)
    gc.fillRect(blueRect)

    gc.fill(Color.red)
    gc.fillRect(redRect)
  }
}

