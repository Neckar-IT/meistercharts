package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.svg.SVGPathParser
import com.meistercharts.resources.svg.SvgPaths

/**
 *
 */
class SvgPathsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "SVG Path on canvas"
  override val description: String = "Paints the parsed SVG paths"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      val paths = SvgPaths.all.map {
        SVGPathParser.from(it).parse()
      }

      meistercharts {
        configure {
          layers.addClearBackground()

          var fill: Color = Color.darkgray

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.scale(4.0, 4.0)
              gc.fill(fill)
              paths
                .chunked(7)
                .forEach { pathsForLine ->
                  gc.saved {
                    pathsForLine.forEach {
                      gc.translate(26.0, 0.0)
                      gc.fill(it)
                    }
                  }

                  gc.translate(0.0, 26.0)
                }
            }
          })

          configurableColor("fill", fill) {
            onChange {
              fill = it
              markAsDirty()
            }
          }
        }
      }

    }
  }
}
