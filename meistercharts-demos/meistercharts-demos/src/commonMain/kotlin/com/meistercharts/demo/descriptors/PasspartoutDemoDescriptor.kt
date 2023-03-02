package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.KeepCenterOnWindowResize
import com.meistercharts.algorithms.KeepOriginOnWindowResize
import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.impl.debugEnabled
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addPasspartout
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Insets

class PasspartoutDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Passpartout"
  override val description: String = "## A passpartout\n with size 10,50,100,150"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        @Zoomed var insets = Insets(10.0, 50.0, 100.0, 150.0)
        @Zoomed var margin = Insets.empty

        val zoomAndTranslationDefaults = FittingWithMargin(insets + margin)

        fun Insets.toStringWithInts(): String {
          return "top=${top.toInt()}, right=${right.toInt()}, bottom=${bottom.toInt()}, left=${left.toInt()}"
        }

        configure {
          chartSupport.pixelSnapSupport.snapConfiguration = SnapConfiguration.None

          layers.addClearBackground()
          val passpartoutLayer = layers.addPasspartout(insets, Color.rgba(0x1a, 0x4d, 0x4d, 0.5)) //Transparent
          layers.addLayer(ContentAreaDebugLayer())
          layers.addText({ _, _ -> listOf("insets: ${insets.toStringWithInts()}", "margin: ${margin.toStringWithInts()}") }, { textColor = Color.gray })

          configurableList("Snap config", SnapConfiguration.None, listOf(SnapConfiguration.None, SnapConfiguration.Both, SnapConfiguration.OnlyX, SnapConfiguration.OnlyY)) {
            onChange {
              chartSupport.pixelSnapSupport.snapConfiguration = it
              markAsDirty()
            }
            converter = {
              it.name
            }
          }

          configurableList("Resize behavior", chartSupport.windowResizeBehaviorProperty.value, listOf(KeepOriginOnWindowResize, KeepCenterOnWindowResize, ResetToDefaultsOnWindowResize)) {
            onChange {
              chartSupport.windowResizeBehaviorProperty.value = it
            }
            converter = {
              it::class.simpleName ?: it.toString()
            }
          }

          configurableInsetsSeparate("Insets") {
            value = insets
            min = 0.0
            max = 200.0
            onChange {
              insets = it
              passpartoutLayer.style.insets = { insets }
              zoomAndTranslationDefaults.marginProvider = { insets + margin }
              markAsDirty()
            }
          }

          configurableColorPickerProvider("Color", passpartoutLayer.style::color) {
          }

          configurableInsetsSeparate("Margin") {
            value = margin
            min = 0.0
            max = 200.0
            onChange {
              margin = it
              passpartoutLayer.style.margin = { margin }
              zoomAndTranslationDefaults.marginProvider = { insets + margin }
              markAsDirty()
            }
          }

          configurableList("Painting Strategy", passpartoutLayer.style.strategy, PasspartoutLayer.availableStrategies) {
            onChange {
              passpartoutLayer.style.strategy = it
              markAsDirty()
            }
            converter = {
              it::class.simpleName ?: it.toString()
            }
          }

        }

        zoomAndTranslationDefaults { zoomAndTranslationDefaults }
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

      }
    }
  }
}
