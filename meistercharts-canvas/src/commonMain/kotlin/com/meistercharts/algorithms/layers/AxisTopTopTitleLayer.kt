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

import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.textService
import com.meistercharts.design.Theme
import com.meistercharts.model.Direction
import com.meistercharts.model.Vicinity
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.DoubleProvider1
import it.neckar.open.i18n.TextService
import it.neckar.open.unit.other.px

/**
 * A layer that paints the axis title for an axis - on the top.
 *
 * This layer must only be used for vertical axis
 */
class AxisTopTopTitleLayer(
  /**
   * Returns the x anchor location
   */
  xAnchorLocation: @Window DoubleProvider1<LayerPaintingContext>,

  /**
   * Returns the y anchor location
   */
  yAnchorLocation: @Window DoubleProvider1<LayerPaintingContext>,

  /**
   * Provides the label for the axis
   */
  titleProvider: AxisTitleProvider,

  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  val configuration: Configuration = Configuration(
    xAnchorLocation, yAnchorLocation, titleProvider
  ).also(additionalConfiguration)

  override val type: LayerType = LayerType.Content

  override fun paintingVariables(): PaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : PaintingVariables {
    var y: Double = Double.NaN
    var x: Double = Double.NaN

    override fun calculate(paintingContext: LayerPaintingContext) {
      val chartCalculator = paintingContext.chartCalculator

      y = configuration.yAnchorLocation(paintingContext)
      x = configuration.xAnchorLocation(paintingContext)
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val i18nConfiguration = paintingContext.i18nConfiguration
    val textService: TextService = paintingContext.chartSupport.textService

    paintingContext.ifDebug(DebugFeature.ShowAnchors) {
      gc.paintMark(paintingVariables.x, paintingVariables.y)
    }

    val title = configuration.titleProvider(textService, i18nConfiguration) ?: return

    gc.font(configuration.titleFont)
    gc.fill(configuration.titleColor())

    gc.fillText(
      title,
      paintingVariables.x,
      paintingVariables.y,
      configuration.anchorDirection(),
      gapHorizontal = configuration.titleGapHorizontal,
      gapVertical = configuration.titleGapVertical,
      maxWidth = configuration.titleMaxWidth()
    )
  }

  class Configuration(
    /**
     * Returns the x anchor location
     */
    var xAnchorLocation: @Window DoubleProvider1<LayerPaintingContext>,

    /**
     * Returns the y anchor location
     */
    var yAnchorLocation: @Window DoubleProvider1<LayerPaintingContext>,

    /**
     * Provides the label for the axis
     */
    var titleProvider: AxisTitleProvider,
  ) {
    var titleGapHorizontal: @px Double = 00.0

    /**
     * The distance between the top of the content area and the title
     */
    var titleGapVertical: @px Double = 15.0

    /**
     * The max width of the title
     */
    var titleMaxWidth: @px DoubleProvider = DoubleProvider.NaN

    /**
     * The anchor direction of the text
     */
    var anchorDirection: () -> Direction = Direction.BottomRight.asProvider()

    /**
     * The color to be used for the title of the axis
     */
    var titleColor: () -> Color = Theme.axisTitleColor().asProvider()

    /**
     * The font that is used for the title
     */
    var titleFont: FontDescriptorFragment = Theme.axisTitleFont()
  }

  companion object {
    /**
     * Creates a new title layer for a value axis.
     */
    fun forAxis(axisLayer: ValueAxisLayer, additionalConfiguration: Configuration.() -> Unit = {}): AxisTopTopTitleLayer {
      return AxisTopTopTitleLayer(
        xAnchorLocation = {
          axisLayer.paintingVariables().axisLineLocation
        },

        yAnchorLocation = {
          when (axisLayer.style.paintRange) {
            AxisStyle.PaintRange.ContentArea -> it.chartCalculator.contentAreaRelative2windowY(0.0)
              .coerceAtLeast(it.chartCalculator.contentViewportMinY()) //do not move above content viewport margin
            AxisStyle.PaintRange.Continuous -> {
              //Paint in the content viewport
              it.chartCalculator.contentViewportMinY()
            }
          }
        },

        titleProvider = { textService, i18nConfiguration ->
          axisLayer.style.titleProvider?.invoke(textService, i18nConfiguration)
        }
      ) {
        titleMaxWidth = DoubleProvider { axisLayer.style.size }
        anchorDirection = {
          when (axisLayer.style.tickOrientation) {
            Vicinity.Inside -> Direction.BottomLeft
            Vicinity.Outside -> Direction.BottomRight
          }
        }

        additionalConfiguration()
      }
    }

    fun forAxis(axisLayer: CategoryAxisLayer, additionalConfiguration: Configuration.() -> Unit = {}): AxisTopTopTitleLayer {
      return AxisTopTopTitleLayer(
        xAnchorLocation = {
          axisLayer.paintingVariables().axisLineLocation
        },

        yAnchorLocation = {
          when (axisLayer.style.paintRange) {
            AxisStyle.PaintRange.ContentArea -> it.chartCalculator.contentAreaRelative2windowY(0.0)
              .coerceAtLeast(it.chartCalculator.contentViewportMinY()) //do not move above content viewport margin
            AxisStyle.PaintRange.Continuous -> {
              //Paint in the content viewport
              it.chartCalculator.contentViewportMinY()
            }
          }
        },

        titleProvider = { textService, i18nConfiguration ->
          axisLayer.style.titleProvider?.invoke(textService, i18nConfiguration)
        }
      ) {
        titleMaxWidth = DoubleProvider { axisLayer.style.size }
        anchorDirection = {
          when (axisLayer.style.tickOrientation) {
            Vicinity.Inside -> Direction.BottomLeft
            Vicinity.Outside -> Direction.BottomRight
          }
        }

        additionalConfiguration()
      }
    }
  }
}
