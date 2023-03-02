package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandlerBroker
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Coordinates
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.gesture.SingleTapGestureSupport
import it.neckar.logging.LoggerFactory

/**
 *
 */
class TouchSingleTapDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Touch: Single Tap"

  override val description: String = """
  """.trimIndent()

  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          this.layers.addClearBackground()

          this.layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var lastTabCoordinates: @Window Coordinates? = null

            val singleTapGestureSupport = SingleTapGestureSupport().also {
              it.onTap {
                logger.debug("Tap detected")
                lastTabCoordinates = it
                markAsDirty()
                EventConsumption.Consumed
              }
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              lastTabCoordinates?.let {
                gc.paintMark(it, color = Color.red)
              }
            }

            override val touchEventHandler: CanvasTouchEventHandler = CanvasTouchEventHandlerBroker().also {
              it.delegate(singleTapGestureSupport)
            }
          })
        }
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.TouchSingleTapDemoDescriptor")
  }
}
