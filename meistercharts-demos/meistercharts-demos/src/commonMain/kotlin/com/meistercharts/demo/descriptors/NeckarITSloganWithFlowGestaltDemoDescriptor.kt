package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.painter.RadialGradient
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.textService
import com.meistercharts.charts.NeckarITSloganWithFlowGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableList

class NeckarITSloganWithFlowGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Neckar IT Slogan With Flow Chart"
  override val description: String = "Neckar IT Slogan With Flow Chart"
  override val category: DemoCategory = DemoCategory.Gestalt

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      val gestalt = NeckarITSloganWithFlowGestalt()

      meistercharts {
        gestalt.configure(this)

        configure {
          val slogans = listOf(
            gestalt.sloganLayer.data.sloganProvider(chartSupport.textService, chartSupport.i18nConfiguration),
            "We make everything flow",
            "Reach to the stars",
            ""
          )
          configurableList("slogan", slogans.first(), slogans) {
            onChange {
              gestalt.sloganLayer.data.sloganProvider = { _, _ -> it }
              this@ChartingDemo.markAsDirty()
            }
          }

          configurableColorPicker("slogan foreground", gestalt.sloganLayer.style::foreground)

          configurableColorPicker("slogan background", gestalt.backgroundLayer.style::background)

          configurableColor("slogan glow color", gestalt.sloganLayer.style.glowGradient.color0) {
            onChange {
              gestalt.sloganLayer.style.glowGradient = RadialGradient(it, gestalt.sloganLayer.style.glowGradient.color1)
              this@ChartingDemo.markAsDirty()
            }
          }

          configurableDouble("glow scaleX", gestalt.sloganLayer.style::glowScaleX) {
            min = 0.1
            max = 10.0
          }

          configurableDouble("glow scaleY", gestalt.sloganLayer.style::glowScaleY) {
            min = 0.1
            max = 10.0
          }

          configurableFont("slogan font", gestalt.sloganLayer.style::sloganFont)

          configurableBoolean("fit font size", gestalt.sloganLayer.style::keepSloganInBounds)

          configurableDouble("slogan offset-y (px)", gestalt.sloganLayer.style::sloganOffsetY) {
            min = -200.0
            max = 200.0
          }

          configurableEnum("Slogan Anchor", gestalt.sloganLayer.style::anchorDirection, enumValues())
        }
      }
    }
  }
}
