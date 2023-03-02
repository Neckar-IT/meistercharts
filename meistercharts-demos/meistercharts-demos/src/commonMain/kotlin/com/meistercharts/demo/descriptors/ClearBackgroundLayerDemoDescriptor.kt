package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.ClearBackgroundLayer
import com.meistercharts.algorithms.layers.FillBackgroundLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.LayerVisibilityAdapter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import it.neckar.open.observable.ObservableBoolean

/**
 * Very simple demo that shows how to clear the background
 */
class ClearBackgroundLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Clear background layer"
  override val description: String = "Clears the background"
  override val category: DemoCategory = DemoCategory.Layers

  private val clearBackground = ObservableBoolean(true)

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {


      meistercharts {
        configure {
          layers.addLayer(FillBackgroundLayer {
            background = Color.orangered
          })
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Background

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.fill(Color.blue)
              gc.fillRect(-10_000.0, -10_000.0, 100_000.0, 100_000.0)
            }
          })

          layers.addLayer(LayerVisibilityAdapter(ClearBackgroundLayer(), { clearBackground.value }))

          configurableBoolean("Clear", clearBackground)
        }
      }
    }
  }
}

