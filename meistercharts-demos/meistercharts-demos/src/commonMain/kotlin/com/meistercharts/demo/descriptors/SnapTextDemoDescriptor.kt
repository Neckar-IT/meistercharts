package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontFamily
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.FontWeight
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.model.Direction
import it.neckar.open.kotlin.lang.fastFor

/**
 */
class SnapTextDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Snapping Text Demo"

  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            var fill: Color = Color.rgb(44, 49, 51) //copied from bug report
            var font: FontDescriptorFragment = FontDescriptorFragment(FontFamily("Open Sans"), size = FontSize(12.0), weight = FontWeight(400)) //copied from demo
            var snapConfiguration: SnapConfiguration = SnapConfiguration.None
            var direction = Direction.TopLeft

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.fill(fill)
              gc.font(font)

              gc.saved {
                gc.translate(100.0, 100.0)
                gc.saved {
                  paintTexts(gc)
                }

                gc.translate(0.0, 100.0)
                gc.saved {
                  paintTexts(gc)
                }

                gc.translate(0.0, 100.5)
                gc.saved {
                  paintTexts(gc)
                }

                gc.translate(0.5, 100.0)
                gc.saved {
                  paintTexts(gc)
                }

                gc.translate(0.1, 100.1)
                gc.saved {
                  paintTexts(gc)
                }

                gc.translate(0.1, 100.1)
                gc.saved {
                  paintTexts(gc, 0.5, 0.5)
                }
              }

              gc.translate(400.0, 100.0)
              40.fastFor {
                gc.translate(0.0, 21.234234234)
                paintTexts(gc)
              }

            }

            private fun paintTexts(gc: CanvasRenderingContext, additionalValueX: @PhysicalPixel Double = 0.0, additionalValueY: @PhysicalPixel Double = 0.0) {
              gc.snapPhysicalTranslation(
                additionalValueX,
                additionalValueY,
                snapX = snapConfiguration.snapX,
                snapY = snapConfiguration.snapY
              )
              gc.paintMark()
              fillText(gc, direction)
            }

            private fun fillText(gc: CanvasRenderingContext, direction: Direction) {
              gc.fillText(gc.translation.toString() + " / Physical: ${gc.translationPhysical}", 0.0, 0.0, direction)
            }
          }
          layers.addLayer(layer)

          configurableEnum("snap", layer::snapConfiguration)
          configurableEnum("direction", layer::direction)

          configurableColorPicker("Text Fill", layer::fill)
          configurableFont("Font", layer::font)

        }
      }
    }
  }
}
