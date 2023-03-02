package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.TransformingChartStateLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.withAdditionalTranslation
import com.meistercharts.algorithms.withContentAreaSize
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.charts.FitContentInViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Distance
import com.meistercharts.model.Insets
import com.meistercharts.model.Size

/**
 */
class CompartmentLayoutDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Compartment Layout"

  override val description: String = "Contains three layer that are layouted by modifying the chart state"

  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        FitContentInViewportGestalt(Insets.of(25.0)).configure(this)

        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          val chartCalculator = chartSupport.chartCalculator

          layers.addLayer(TransformingChartStateLayer(MyDebugLayer(Color.aquamarine)) { chartState ->
            chartState.withContentAreaSize(chartState.contentAreaSize.let {
              Size(it.width, it.height / 3.0)
            })
          })

          layers.addLayer(TransformingChartStateLayer(MyDebugLayer(Color.blue)) {
            @ContentArea val contentAreaSize = chartCalculator.chartState.contentAreaSize
            @ContentArea val height = contentAreaSize.height / 3.0

            @Zoomed val additionalTranslation = Distance.of(0.0, chartCalculator.contentArea2zoomedY(height))

            it.withContentAreaSize(Size(contentAreaSize.width, height)).withAdditionalTranslation(additionalTranslation)
          })

          layers.addLayer(TransformingChartStateLayer(MyDebugLayer(Color.green)) {
            @ContentArea val contentAreaSize = chartCalculator.chartState.contentAreaSize
            @ContentArea val height = contentAreaSize.height / 3.0

            @Zoomed val additionalTranslation = Distance.of(0.0, chartCalculator.contentArea2zoomedY(height) * 2)
            it.withContentAreaSize(Size(contentAreaSize.width, height)).withAdditionalTranslation(additionalTranslation)
          })
        }
      }
    }
  }
}

class MyDebugLayer(val color: Color) : ContentAreaDebugLayer({
  lines(color)
}) {
  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.paintMark(color = color)

    gc.saved {
      val chartCalculator = paintingContext.chartCalculator
      gc.translate(chartCalculator.contentAreaRelative2windowX(0.0), chartCalculator.contentAreaRelative2windowY(0.0))
      gc.paintMark(color = color)
    }

    super.paint(paintingContext)
  }
}
