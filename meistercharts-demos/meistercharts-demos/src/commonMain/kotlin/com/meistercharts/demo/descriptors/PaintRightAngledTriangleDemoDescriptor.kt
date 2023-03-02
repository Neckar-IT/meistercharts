package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.LocationType
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.section
import com.meistercharts.model.RightTriangleType

class PaintRightAngledTriangleDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Triangles Right Angled"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content
            var x: Double = 100.0
            var y: Double = 100.0

            var width = 100.0
            var height = 150.0

            var triangleType = RightTriangleType.MissingCornerInFirstQuadrant

            var xLocationType: LocationType = LocationType.Origin
            var yLocationType: LocationType = LocationType.Origin
            var lineWidth: Double = 3.0


            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.lineWidth = lineWidth
              gc.strokeTriangleRightAngled(x, y, width, height, triangleType, xLocationType, yLocationType)

              gc.lineWidth = 1.0
              gc.paintMark(x, y, color = Color.orange)
            }
          }

          layers.addLayer(layer)

          section("Simple Triangle")

          section("Advanced Triangle")
          configurableDouble("x", layer::x) {
            max = 1000.0
          }
          configurableDouble("y", layer::y) {
            max = 1000.0
          }
          configurableDouble("width", layer::width) {
            max = 400.0
          }
          configurableDouble("height", layer::height) {
            max = 400.0
          }

          configurableEnum("Triangle Type", layer::triangleType)
          configurableEnum("X location type", layer::xLocationType)
          configurableEnum("y location type", layer::yLocationType)

          configurableDouble("Line Width", layer::lineWidth) {
            max = 10.0
          }
        }
      }
    }
  }
}
