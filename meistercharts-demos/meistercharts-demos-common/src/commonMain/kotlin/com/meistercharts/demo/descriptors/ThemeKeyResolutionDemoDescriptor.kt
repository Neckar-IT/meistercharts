/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.paintable.ButtonColorProvider
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.design.Theme
import com.meistercharts.design.ThemeKey
import com.meistercharts.font.FontDescriptorFragment
import it.neckar.geometry.Direction
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.MultiProvider

/**
 */
class ThemeKeyResolutionDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("885c9bd1-2151-476d-920b-db6753855fe0")
  override val name: String = "Theme"
  override val category: DemoCategory = DemoCategory.ShowCase
  override val description: String = "Tests the resolution of theme keys"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
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
      paintThemeColor(gc, Theme.secondaryBackgroundColor)
      gc.translate(0.0, deltaY)
      paintThemeColor(gc, Theme.primaryBackgroundColor)
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
      paintButtonColors(gc, Theme.primaryButtonForegroundColors.resolve())
      gc.translate(0.0, deltaY)
      paintTitle(gc, "Primary Button Background")
      gc.translate(0.0, deltaY)
      paintButtonColors(gc, Theme.primaryButtonBackgroundColors.resolve())

      gc.translate(0.0, deltaY)
      paintTitle(gc, "Secondary Button Foreground")
      gc.translate(0.0, deltaY)
      paintButtonColors(gc, Theme.secondaryButtonForegroundColors.resolve())
      gc.translate(0.0, deltaY)
      paintTitle(gc, "Secondary Button Background")
      gc.translate(0.0, deltaY)
      paintButtonColors(gc, Theme.secondaryButtonBackgroundColors.resolve())

      gc.translate(0.0, deltaY)
      paintTitle(gc, "Chart Colors")
      gc.translate(0.0, deltaY)
      paintColorsRow(gc, Theme.chartColors.resolve())

      //Enum Chart colors
      gc.translate(0.0, deltaY)
      paintTitle(gc, "Enum Colors")
      gc.translate(0.0, deltaY)
      paintColorsRow(gc, Theme.enumColors.resolve())

    }

    private fun paintStateColors(gc: CanvasRenderingContext) {
      gc.saved {
        paintColorRect(gc, Theme.State.ok.resolve())
        gc.translate(25.0, 0.0)
        paintColorRect(gc, Theme.State.warning.resolve())
        gc.translate(25.0, 0.0)
        paintColorRect(gc, Theme.State.error.resolve())
        gc.translate(25.0, 0.0)
        paintColorRect(gc, Theme.State.unknown.resolve())
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
      paintColorRectWithLabel(gc, themeKey.resolve(), themeKey.id)
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
        val theFont = themeFont.resolve()
        gc.font(theFont)
        gc.fill(Color.black)
        gc.fillText("\"${themeFont.id}\": $theFont", 0.0, 0.0, Direction.TopLeft)
      }
    }
  }
}
