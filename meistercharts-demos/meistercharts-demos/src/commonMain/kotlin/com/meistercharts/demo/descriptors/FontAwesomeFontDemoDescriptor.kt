package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontFamily
import com.meistercharts.canvas.FontSize
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 *
 */
class FontAwesomeFontDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Font Awesome"

  //language=HTML
  override val description: String = """<h3>FontAwesome Icon</h3>
    |Adds a text with the font family "FontAwesome"
    |
  """.trimMargin()
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addTextUnresolved("\uF002 \uF24E") {
            font = FontDescriptorFragment(FontFamily("FontAwesome"), FontSize(120.0))
          }
        }
      }
    }
  }
}
