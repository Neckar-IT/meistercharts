package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.paintable.ButtonColorProvider
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.design.Theme
import com.meistercharts.design.ThemeKey
import com.meistercharts.model.Direction
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.MultiProvider

/**
 */
class ThemeDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Theme"
  override val category: DemoCategory = DemoCategory.ShowCase
  override val description: String = "Tests the resolution of theme keys"

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(MyThemeLayer())
        }
      }
    }
  }

  private class MyThemeLayer : AbstractLayer() {
    override val type: LayerType = LayerType.Content

    override fun paint(paintingContext: LayerPaintingContext) {
      val gc = paintingContext.gc

      gc.translate(25.0, 0.0)
      val deltaY = 25.0

      gc.translate(0.0, deltaY)
      paintThemeFont(gc, Theme.axisTitleFont)
      gc.translate(0.0, deltaY)
      paintThemeColor(gc, Theme.axisTitleColor)
      gc.translate(0.0, deltaY)
      paintThemeFont(gc, Theme.axisTickFont)
      gc.translate(0.0, deltaY)
      paintThemeColor(gc, Theme.axisTickColor)
      gc.translate(0.0, deltaY)
      paintThemeColor(gc, Theme.axisLineColor)
      gc.translate(0.0, deltaY)
      paintThemeColor(gc, Theme.darkBackgroundColor)
      gc.translate(0.0, deltaY)
      paintThemeColor(gc, Theme.lightBackgroundColor)
      gc.translate(0.0, deltaY)
      paintThemeColor(gc, Theme.backgroundColorActive)
      gc.translate(0.0, deltaY)
      paintThemeColor(gc, Theme.inactiveElementBorderColor)
      gc.translate(0.0, deltaY)
      paintThemeColor(gc, Theme.crossWireLineColor)
      gc.translate(0.0, deltaY)
      paintThemeFont(gc, Theme.buttonFont)

      //State colors
      gc.translate(0.0, deltaY)
      paintTitle(gc, "State Colors")
      gc.translate(0.0, deltaY)
      paintStateColors(gc)

      gc.translate(0.0, deltaY)
      paintTitle(gc, "Primary Button Foreground")
      gc.translate(0.0, deltaY)
      paintButtonColors(gc, Theme.primaryButtonForegroundColors())
      gc.translate(0.0, deltaY)
      paintTitle(gc, "Primary Button Background")
      gc.translate(0.0, deltaY)
      paintButtonColors(gc, Theme.primaryButtonBackgroundColors())

      gc.translate(0.0, deltaY)
      paintTitle(gc, "Secondary Button Foreground")
      gc.translate(0.0, deltaY)
      paintButtonColors(gc, Theme.secondaryButtonForegroundColors())
      gc.translate(0.0, deltaY)
      paintTitle(gc, "Secondary Button Background")
      gc.translate(0.0, deltaY)
      paintButtonColors(gc, Theme.secondaryButtonBackgroundColors())

      //Paint the chart colors
      gc.translate(0.0, deltaY)
      paintTitle(gc, "Chart Colors")
      gc.translate(0.0, deltaY)
      paintColorsRow(gc, Theme.chartColors())

      //Enum Chart colors
      gc.translate(0.0, deltaY)
      paintTitle(gc, "Enum Colors")
      gc.translate(0.0, deltaY)
      paintColorsRow(gc, Theme.enumColors())

    }

    private fun paintStateColors(gc: CanvasRenderingContext) {
      gc.saved {
        paintColorRect(gc, Theme.State.ok())
        gc.translate(25.0, 0.0)
        paintColorRect(gc, Theme.State.warning())
        gc.translate(25.0, 0.0)
        paintColorRect(gc, Theme.State.error())
        gc.translate(25.0, 0.0)
        paintColorRect(gc, Theme.State.unknown())
      }
    }

    /**
     * Attention: Contains *additional* translation
     */
    private fun paintTitle(gc: CanvasRenderingContext, title: String) {
      //Additional translation!
      gc.translate(0.0, 8.0)
      gc.fill(Color.black)
      gc.fillText(title, 0.0, 0.0, Direction.TopLeft)
    }

    private fun paintButtonColors(gc: CanvasRenderingContext, buttonColorProvider: ButtonColorProvider) {
      gc.saved {
        paintColorRect(gc, buttonColorProvider(ButtonState(true, false)))
        gc.translate(25.0, 0.0)
        paintColorRect(gc, buttonColorProvider(ButtonState(true, true)))

        gc.translate(25.0, 0.0)
        paintColorRect(gc, buttonColorProvider(ButtonState(true, true, true)))
        gc.translate(25.0, 0.0)
        paintColorRect(gc, buttonColorProvider(ButtonState(true, false, true)))

        gc.translate(25.0, 0.0)
        paintColorRect(gc, buttonColorProvider(ButtonState(true, false, false, true)))
        gc.translate(25.0, 0.0)
        paintColorRect(gc, buttonColorProvider(ButtonState(true, true, false, true)))

        gc.translate(25.0, 0.0)
        paintColorRect(gc, buttonColorProvider(ButtonState(true, false, false, false, true)))
        gc.translate(25.0, 0.0)
        paintColorRect(gc, buttonColorProvider(ButtonState(true, true, false, false, true)))
      }
    }

    private fun paintThemeColor(gc: CanvasRenderingContext, themeKey: ThemeKey<Color>) {
      paintColorRectWithLabel(gc, themeKey(), themeKey.id)
    }

    private fun paintColorsRow(gc: CanvasRenderingContext, chartColors: MultiProvider<Any, Color>) {
      gc.saved {
        10.fastFor { index ->
          paintColorRect(gc, chartColors.valueAt(index))
          gc.translate(25.0, 0.0)
        }
      }
    }

    private fun paintColorRectWithLabel(gc: CanvasRenderingContext, color: Color, displayName: String) {
      paintColorRect(gc, color)
      gc.fill(Color.black)
      gc.fillText("\"$displayName\": $color", 40.0, 0.0, Direction.TopLeft)
    }

    private fun paintColorRect(gc: CanvasRenderingContext, color: Color) {
      gc.fill(color)
      gc.fillRect(0.0, 0.0, 20.0, 20.0)
      gc.stroke(Color.black)
      gc.strokeRect(0.0, 0.0, 20.0, 20.0)
    }

    private fun paintThemeFont(gc: CanvasRenderingContext, themeFont: ThemeKey<FontDescriptorFragment>) {
      gc.saved {
        val theFont = themeFont()
        gc.font(theFont)
        gc.fill(Color.black)
        gc.fillText("\"${themeFont.id}\": $theFont", 0.0, 0.0, Direction.TopLeft)
      }
    }
  }
}
