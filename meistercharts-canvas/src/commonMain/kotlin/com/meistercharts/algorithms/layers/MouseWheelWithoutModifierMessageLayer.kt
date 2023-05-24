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

import com.meistercharts.algorithms.layers.MouseWheelWithoutModifierMessageLayer.Companion.create
import com.meistercharts.algorithms.layers.text.LinesProvider
import com.meistercharts.algorithms.layers.text.TextPainter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.LineSpacing
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.textService
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.ModifierCombination
import com.meistercharts.events.MouseWheelEvent
import com.meistercharts.model.Direction
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.style.BoxStyle
import it.neckar.open.i18n.TextKey
import it.neckar.open.observable.ObservableBoolean
import kotlin.time.Duration.Companion.milliseconds

/**
 * Shows a hint that a modifier needs to be pressed for scrolling
 *
 */
class MouseWheelWithoutModifierMessageLayer(
  /**
   * If the messages is currently visible
   */
  messageVisible: ObservableBoolean = ObservableBoolean(),
  /**
   * Provides the texts
   */
  linesProvider: LinesProvider,

  /**
   * Additional configuration
   */
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  val configuration: Configuration = Configuration(messageVisible, linesProvider).also(additionalConfiguration)

  private val messagePainter = TextPainter()

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    //show background
    gc.fill(configuration.backgroundFill)
    gc.fillRect(0.0, 0.0, gc.width, gc.height)

    val texts = configuration.linesProvider(paintingContext.chartSupport.textService, paintingContext.i18nConfiguration)

    gc.font(configuration.font)
    gc.translateToCenter()
    messagePainter.paintText(
      gc,
      texts,
      configuration.textColor,
      configuration.boxStyle,
      configuration.lineSpacing,
      HorizontalAlignment.Center,
      configuration.anchorDirection,
      configuration.anchorGapHorizontal,
      configuration.anchorGapVertical,
    )
  }

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
      if (event.modifierCombination == ModifierCombination.None) {
        configuration.messageVisible.value = true
        chartSupport.markAsDirty(DirtyReason.UserInteraction)
        return EventConsumption.Consumed
      }

      return EventConsumption.Ignored
    }
  }

  /**
   * Style configuration for the layer
   */
  @ConfigurationDsl
  open class Configuration(
    /**
     * If the messages is currently visible
     */
    val messageVisible: ObservableBoolean,
    /**
     * Provides the texts
     */
    val linesProvider: LinesProvider,
  ) {
    /**
     * The color of the text
     */
    var textColor: Color = Color.white

    /**
     * The background fill that is painted over all other content
     */
    var backgroundFill: Color = Color.rgba(0, 0, 0, 0.6)

    /**
     * The style for the box (background fill + border stroke)
     */
    var boxStyle: BoxStyle = BoxStyle()

    /**
     * Describes the font
     */
    var font: FontDescriptorFragment = FontDescriptorFragment(24.0)

    /**
     * The line spacing
     */
    var lineSpacing: LineSpacing = LineSpacing.Single

    /**
     * The location where the message is painted
     */
    var anchorDirection: Direction = Direction.Center

    var anchorGapHorizontal: Double = 0.0
    var anchorGapVertical: Double = 0.0
  }

  companion object {
    val textKeyUseCtrlZoomHorizontally: TextKey = TextKey("useCtrlToZoomHorizontally", "Use ctrl + scroll to zoom the chart horizontally")
    val textKeyUseShiftZoomVertically: TextKey = TextKey("useShiftToZoomVertically", "Use shift + scroll to zoom the chart vertically")
    val textKeyUseCtrlZoom: TextKey = TextKey("ctrl.to.zoom", "Use ctrl + scroll to zoom the map")

    val defaultTextKeys: List<TextKey> = listOf(
      textKeyUseCtrlZoomHorizontally,
      textKeyUseShiftZoomVertically
    )

    fun create(
      lines: List<TextKey> = defaultTextKeys,
      additionalConfiguration: Configuration.() -> Unit = {},
    ): HideAfterTimeoutLayer<MouseWheelWithoutModifierMessageLayer> {
      val visible = ObservableBoolean()
      val layer = MouseWheelWithoutModifierMessageLayer(visible, { textService, i18nConfiguration ->
        lines.map {
          textService[it, i18nConfiguration]
        }
      }, additionalConfiguration)
        .visibleIf(visible, true)
        .autoHideAfter(2500.milliseconds)
      return layer
    }
  }
}

/**
 * Shows the layer that warns if wheel is used without modifiers
 */
fun Layers.addMouseWheelWithoutModifierHint(
  lines: List<TextKey> = MouseWheelWithoutModifierMessageLayer.defaultTextKeys,
  additionalConfiguration: MouseWheelWithoutModifierMessageLayer.Configuration.() -> Unit = {},
  ): HideAfterTimeoutLayer<MouseWheelWithoutModifierMessageLayer> {

  val layer = create(lines, additionalConfiguration)
  addLayer(layer)
  return layer
}
