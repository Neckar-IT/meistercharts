package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.textService
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import it.neckar.open.i18n.Locale
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.resolve.MapBasedTextResolver
import it.neckar.open.i18n.resolve.SimpleMapBasedTextResolver

/**
 * Demos that visualizes the functionality of the FPS layer
 */
class TextLayerI18nDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Text I18n"
  override val description: String = "## How to show text"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val textKey0 = TextKey("box.right.center", "Box right center")
          val translated0 = "Right Center Translated"

          val textKey1 = TextKey("box.left.center-no-translation", "Box Left center - no translation")

          val textKey2 = TextKey("box.top.center-with-locales", "Box top center - with locales")

          chartSupport.textService.addTextResolverAtFirst(MapBasedTextResolver().apply {
            setText(Locale.US, textKey2, "TOP-US")
            setText(Locale.Germany, textKey2, "TOP-De")
          })

          chartSupport.textService.addTextResolverAtFirst(SimpleMapBasedTextResolver().apply {
            setText(textKey0, translated0)
          })


          layers.addText(textKey0) {
            anchorDirection = Direction.CenterRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
          }
          layers.addText(textKey1) {
            anchorDirection = Direction.CenterLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
          }
          layers.addText(textKey2) {
            anchorDirection = Direction.TopCenter
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
          }
        }
      }
    }
  }
}
