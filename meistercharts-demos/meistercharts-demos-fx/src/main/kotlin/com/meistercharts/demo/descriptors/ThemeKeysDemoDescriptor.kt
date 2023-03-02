package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.design.Theme
import com.meistercharts.design.ThemeKey
import com.meistercharts.model.Direction
import it.neckar.open.collections.fastForEachIndexed
import kotlin.reflect.full.memberProperties

/**
 *
 */
class ThemeKeysDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Theme Keys"
  override val description: String = "Shows all theme keys. Is platform dependent because it uses reflection"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {

          val keys = Theme::class.memberProperties.map {
            (it.get(Theme) as ThemeKey<*>)
          }

          layers.addClearBackground()
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(10.0, 10.0)

              keys.fastForEachIndexed { index, key ->
                val value = key()
                val id = key.id


                gc.saved {
                  gc.translate(0.0, index * 20.0)

                  if (value is Color) {
                    gc.fill(value)
                  }
                  if (value is FontDescriptorFragment) {
                    gc.font(value)
                  }

                  gc.fillText("$id: $value", 0.0, 0.0, Direction.TopLeft)
                }
              }
            }
          })
        }
      }
    }
  }
}
