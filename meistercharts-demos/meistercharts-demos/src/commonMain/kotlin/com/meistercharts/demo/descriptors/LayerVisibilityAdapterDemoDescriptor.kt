package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.TextLayer
import com.meistercharts.algorithms.layers.toolbar.ToolbarButtonFactory
import com.meistercharts.algorithms.layers.toolbar.ToolbarLayer
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import it.neckar.open.observable.ObservableBoolean
import com.meistercharts.resources.Icons

class LayerVisibilityAdapterDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Toggle Layer Visibility"
  override val description: String = "A button can be used to toggle the visibility of a layer"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val textLayerVisible = ObservableBoolean(true)

        configure {
          layers.addClearBackground()
          layers.addLayer(TextLayer({ _, _ -> listOf("Hello World") }).visibleIf(textLayerVisible))

          val toggleButton = ToolbarButtonFactory().toggleButton(Icons::noLegend, Icons::legend).also {
            textLayerVisible.bind(it.selectedProperty)
          }

          val toolbarLayer = ToolbarLayer(
            listOf(
              toggleButton
            )
          )
          layers.addLayer(toolbarLayer)
        }
      }
    }
  }
}
