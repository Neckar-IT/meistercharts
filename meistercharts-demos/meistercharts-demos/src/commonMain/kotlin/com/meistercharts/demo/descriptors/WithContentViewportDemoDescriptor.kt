package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.translate
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.strokeRectCoordinates
import com.meistercharts.charts.FitContentInViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.model.Insets

/**
 */
class WithContentViewportDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "With Content Viewport"

  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val contentViewportGestalt = FitContentInViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this@meistercharts)

        configure {
          layers.addClearBackground()

          val layer0 = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartState = paintingContext.chartState
              val chartCalculator = paintingContext.chartCalculator

              gc.stroke(Color.red)

              gc.strokeRectCoordinates(
                x0 = chartCalculator.contentViewportMinX(),
                y0 = chartCalculator.contentViewportMinY(),
                x1 = chartCalculator.contentViewportMaxX(),
                y1 = chartCalculator.contentViewportMaxY(),
              )

              //Paint the content area - but only within the viewport

              @Window val x = chartCalculator.contentAreaRelative2windowXInViewport(0.0)
              @Window val y = chartCalculator.contentAreaRelative2windowYInViewport(0.0)

              @Window val x2 = chartCalculator.contentAreaRelative2windowXInViewport(1.0)
              @Window val y2 = chartCalculator.contentAreaRelative2windowYInViewport(1.0)

              gc.fill(Color.green.withAlpha(0.3))
              gc.fillRectCoordinates(x, y, x2, y2)
            }
          }

          val layer1 = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartState = paintingContext.chartState
              val chartCalculator = paintingContext.chartCalculator

              gc.stroke(Color.red)

              gc.strokeRectCoordinates(
                x0 = chartCalculator.contentViewportMinX(),
                y0 = chartCalculator.contentViewportMinY(),
                x1 = chartCalculator.contentViewportMaxX(),
                y1 = chartCalculator.contentViewportMaxY(),
              )

              //Paint the content area - but only within the viewport

              @Window val x = chartCalculator.contentAreaRelative2windowXInViewport(0.0)
              @Window val y = chartCalculator.contentAreaRelative2windowYInViewport(0.0)

              @Window val x2 = chartCalculator.contentAreaRelative2windowXInViewport(1.0)
              @Window val y2 = chartCalculator.contentAreaRelative2windowYInViewport(1.0)

              gc.fill(Color.green.withAlpha(0.3))
              gc.fillRectCoordinates(x, y, x2, y2)
            }
          }
          layers.addLayer(layer0)
          layers.addLayer(layer1.translate())

          configurableInsetsSeparate("Content Viewport", contentViewportGestalt.contentViewportMarginProperty)
        }
      }
    }
  }
}
