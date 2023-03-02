package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.paintable.ObjectFit
import com.meistercharts.canvas.paintable.TankPaintable
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Direction

/**
 * A demo for the [TankPaintable]
 */
class TankDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Water Tank"
  override val category: DemoCategory = DemoCategory.Paintables

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      val tankPaintable = TankPaintable()

      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val targetTankSize = tankPaintable.size.scaleToHeight(800.0)
              gc.saved {
                tankPaintable.paintInBoundingBox(paintingContext, 5.0, 5.0, Direction.TopLeft, 0.0, 0.0, targetTankSize.width, targetTankSize.height, ObjectFit.Contain)
              }
            }
          })
        }

        configurableDouble("Fill level", tankPaintable::fillLevel) {
          min = 0.0
          max = 1.0
        }
      }
    }
  }
}
