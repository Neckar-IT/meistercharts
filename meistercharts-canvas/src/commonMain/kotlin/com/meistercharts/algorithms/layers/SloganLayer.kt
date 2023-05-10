/**
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
package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.RadialGradient
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.guessFontSize
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.textService
import com.meistercharts.design.Theme
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import com.meistercharts.model.VerticalAlignment
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import kotlin.math.max

/**
 * Displays a slogan
 */
class SloganLayer(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  val style: Style = Style().also(styleConfiguration)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val textService = paintingContext.chartSupport.textService
    val i18nConfiguration = paintingContext.chartSupport.i18nConfiguration

    val sloganText = data.sloganProvider(textService, i18nConfiguration)
    if (sloganText.isEmpty()) {
      return
    }

    gc.font(style.sloganFont)

    var textSize: Size = gc.calculateTextSize(sloganText)
    if (style.keepSloganInBounds) {
      val maxTextSize = Size(gc.width * style.maxPercentage, gc.height * style.maxPercentage)

      //Calculate the max font size
      textSize = gc.guessFontSize(sloganText, maxTextSize, FontSize.XXS) ?: return
    }

    //glow effect
    val glowWidth = textSize.width * style.glowScaleX
    val glowHeight = textSize.height * style.glowScaleY

    //Calculate the optimal translate y
    @px val optimalTranslateY = gc.height / 2.0 + style.sloganOffsetY
    val minTranslateY = gc.getFontMetrics().accentLine / 2.0

    gc.translate(gc.width / 2.0, max(optimalTranslateY, minTranslateY))

    gc.saved {
      //move to the Y center of the text
      when (style.anchorDirection.verticalAlignment) {
        VerticalAlignment.Top -> gc.translate(0.0, textSize.height / 2.0)
        VerticalAlignment.Baseline -> gc.translate(0.0, gc.getFontMetrics().pLine - textSize.height / 2.0)
        VerticalAlignment.Bottom -> gc.translate(0.0, -textSize.height / 2.0)
        VerticalAlignment.Center -> {
        }
      }

      gc.fill(
        style.glowGradient
          .toCanvasPaint(0.0, 0.0, glowWidth / 2.0, glowHeight / 2.0)
      )

      //Turn the circle-lik gradient into an ellipse
      val scaleY = 1.0 / glowWidth * glowHeight
      gc.scale(1.0, scaleY)
      val scaledHeight = glowHeight / scaleY
      gc.fillRect(-glowWidth / 2.0, -scaledHeight / 2.0, glowWidth, scaledHeight)
    }

    gc.fill(style.foreground)
    gc.fillText(sloganText, 0.0, 0.0, style.anchorDirection)
  }

  class Data(
    var sloganProvider: (textService: TextService, i18nConfiguration: I18nConfiguration) -> String = { _, _ -> "Data brought to life" }
  )

  class Style {
    /**
     * The anchor direction
     */
    var anchorDirection: Direction = Direction.BaseLineCenter

    /**
     * The max percentage (width and height) before the font size is reduced
     */
    var maxPercentage: @pct Double = 0.75

    /**
     * The vertical offset of the slogan text applied to the center of the layer.
     *
     * A negative value moves the slogan towards the top a positive value moves it towards the bottom of the layer.
     *
     * The offset moves the *baseline* of the text.
     */
    var sloganOffsetY: @Zoomed Double = -50.0

    /**
     * The color to be used as foreground
     */
    var foreground: Color = Color.white

    /**
     * The gradient that is used as glow background
     */
    var glowGradient: RadialGradient = RadialGradient(
      Color.rgba(255, 255, 255, 0.07),
      Color.rgba(255, 255, 255, 0.0)
    )

    /**
     * The width of the glow equals the width of the slogan text times this value.
     */
    var glowScaleX: Double = 1.2

    /**
     * The height of the glow equals the height of the slogan text times this value.
     */
    var glowScaleY: Double = 1.2

    /**
     * The font to be used for the slogan text
     */
    var sloganFont: FontDescriptorFragment = Theme.sloganFont()

    /**
     * Whether to adjust the font size of the slogan to ensure that it does not break out of this layer's bounds
     */
    var keepSloganInBounds: Boolean = true
  }
}
