package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.ValueAxisHudLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section
import com.meistercharts.model.Direction
import com.meistercharts.style.BoxStyle
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.kotlin.lang.enumEntries
import it.neckar.open.provider.CoordinatesProvider1
import it.neckar.open.provider.MultiDoublesProvider
import it.neckar.open.provider.MultiProvider

class ValueAxisHudLayerZOrderDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value axis HUD - Z Order"

  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      meistercharts {
        configure {

          val config = object {
            var locationX: Double = 100.0
            var locationY: Double = 100.0
            var anchorDirection: Direction = Direction.CenterLeft
            var font = FontDescriptorFragment.empty

            var z0 = 10.0
            var z1 = 12.0
            var z2 = 13.0
          }

          layers.addClearBackground()
          val hudLayer = ValueAxisHudLayer(
            locations = object : CoordinatesProvider1<LayerPaintingContext> {
              override fun size(param1: LayerPaintingContext): Int {
                return 3
              }

              override fun xAt(index: Int, param1: LayerPaintingContext): Double {
                return when (index) {
                  0 -> config.locationX
                  else -> 100.0
                }
              }

              override fun yAt(index: Int, param1: LayerPaintingContext): Double {
                return when (index) {
                  0 -> config.locationY
                  1 -> 120.0
                  2 -> 130.0
                  else -> 200.0
                }
              }
            },
            labels = { index, _ -> listOf("Da Label $index", decimalFormat.format(config.locationX)) }

          ) {
            anchorDirections = MultiProvider { config.anchorDirection }
            textFonts = MultiProvider { config.font }

            zOrder = MultiDoublesProvider {
              when (it) {
                0 -> config.z0
                1 -> config.z1
                2 -> config.z2
                else -> Double.NaN
              }
            }
          }

          layers.addLayer(
            hudLayer
          )

          section("Z-Orders")

          configurableDouble("Z0", config::z0) {
            max = 100.0
          }
          configurableDouble("Z1", config::z1) {
            max = 100.0
          }
          configurableDouble("Z2", config::z2) {
            max = 100.0
          }

          section("Locations")


          configurableDouble("x", config::locationX) {
            max = 1000.0
          }
          configurableDouble("y", config::locationY) {
            max = 1000.0
          }

          configurableEnum("Anchor Direction", config::anchorDirection, enumEntries())

          configurableList("Box Style", hudLayer.configuration.boxStyles.valueAt(0), listOf(BoxStyle.black, BoxStyle.modernGray, BoxStyle.gray, BoxStyle.none)) {
            onChange {
              hudLayer.configuration.boxStyles = MultiProvider.always(it)
              this@ChartingDemo.markAsDirty()
            }
          }

          configurableDouble("Triangle width", hudLayer.configuration.arrowHeadWidth.valueAt(0)) {
            max = 40.0
            onChange {
              hudLayer.configuration.arrowHeadWidth = MultiDoublesProvider.always(it)
            }
          }
          configurableDouble("Triangle height", hudLayer.configuration.arrowHeadLength.valueAt(0)) {
            max = 40.0
            onChange {
              hudLayer.configuration.arrowHeadLength = MultiDoublesProvider.always(it)
            }
          }

          configurableColor("Text", hudLayer.configuration.textColors.valueAt(0)) {
            onChange {
              hudLayer.configuration.textColors = MultiProvider.always(it)
            }
          }

          configurableFont("Label", config::font) {
          }
        }
      }
    }
  }
}
