package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.KeepCenterOnWindowResize
import com.meistercharts.algorithms.KeepOriginOnWindowResize
import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.impl.debugEnabled
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

class ZoomAndTranslationDefaults10PctMarginDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Zoom + Translation Defaults: 10% Margin"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
          layers.addTextUnresolved("Margin: 10%", Color.gray)
        }

        zoomAndTranslationDefaults { ZoomAndTranslationDefaults.tenPercentMargin }

        zoomAndTranslationModifier {
          minZoom(0.1, 0.1)
          maxZoom(5.0, 5.0)
          contentAlwaysBarelyVisible()

          //If enabled only positive movements are allowed
          //.withOnlyPositivePanModifier()
          //.withDisabledPanningX()
          //.withDisabledZoomingAndPanning()
          debugEnabled()
        }

        declare { layerSupport ->
          section("Resize") {
            comboBox("Behavior", layerSupport.chartSupport.windowResizeBehaviorProperty, listOf(KeepOriginOnWindowResize, KeepCenterOnWindowResize, ResetToDefaultsOnWindowResize)) {
              it::class.simpleName ?: it.toString()
            }
          }
        }
      }
    }
  }
}
