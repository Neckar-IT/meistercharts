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

import com.meistercharts.algorithms.layers.text.LinesProvider
import com.meistercharts.algorithms.layers.text.TextPainter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.LineSpacing
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.registerDirtyListener
import com.meistercharts.canvas.textService
import com.meistercharts.canvas.timerSupport
import com.meistercharts.model.Direction
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.ModifierCombination
import com.meistercharts.events.MouseWheelEvent
import it.neckar.open.i18n.TextKey
import it.neckar.open.observable.ObservableBoolean
import com.meistercharts.style.BoxStyle
import kotlin.time.Duration.Companion.milliseconds

/**
 * Shows a hint that a modifier needs to be pressed for scrolling
 *
 */
class ScrollWithoutModifierMessageLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  val style: Style = Style().also(styleConfiguration)

  private val messagePainter = TextPainter()

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    //show background
    gc.fill(style.backgroundFill)
    gc.fillRect(0.0, 0.0, gc.width, gc.height)

    val texts = data.linesProvider(paintingContext.chartSupport.textService, paintingContext.i18nConfiguration)

    gc.font(style.font)
    gc.translateToCenter()
    messagePainter.paintText(
      gc,
      texts,
      style.textColor,
      style.boxStyle,
      style.lineSpacing,
      HorizontalAlignment.Center,
      style.anchorDirection,
      style.anchorGapHorizontal,
      style.anchorGapVertical,
    )
  }

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
      if (event.modifierCombination == ModifierCombination.None) {
        data.messageVisible.value = true
        chartSupport.markAsDirty(DirtyReason.UserInteraction)
        return EventConsumption.Consumed
      }

      return EventConsumption.Ignored
    }
  }

  class Data(
    /**
     * If the messages is currently visible
     */
    val messageVisible: ObservableBoolean,
    /**
     * Provides the texts
     */
    val linesProvider: LinesProvider
  )

  /**
   * Style configuration for the layer
   */
  @ConfigurationDsl
  open class Style {
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
  }
}

/**
 * Shows the layer that warns if wheel is used without modifiers
 */
fun Layers.addScrollWithoutModifierHint(
  chartSupport: ChartSupport,
  lines: List<TextKey> = listOf(
    ScrollWithoutModifierMessageLayer.textKeyUseCtrlZoomHorizontally,
    ScrollWithoutModifierMessageLayer.textKeyUseShiftZoomVertically
  ),
  styleConfiguration: ScrollWithoutModifierMessageLayer.Style.() -> Unit = {}
) {
  val visible = ObservableBoolean() //TODO bind dirty state
  visible.registerDirtyListener(chartSupport, DirtyReason.Visibility)
  addLayer(
    ScrollWithoutModifierMessageLayer(ScrollWithoutModifierMessageLayer.Data(visible) { textService, i18nConfiguration ->
      lines.map {
        textService[it, i18nConfiguration]
      }
    }, styleConfiguration)
      .visibleIf(visible, true)
      .autoHideAfter(2500.milliseconds, chartSupport.timerSupport)
  )
}
