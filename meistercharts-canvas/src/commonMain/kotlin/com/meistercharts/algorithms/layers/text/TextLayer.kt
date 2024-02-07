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
package com.meistercharts.algorithms.layers.text

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.text.LineSpacing
import com.meistercharts.canvas.textService
import com.meistercharts.color.Color
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.geometry.BasePointProvider
import com.meistercharts.geometry.DirectionBasedBasePointProvider
import com.meistercharts.model.Anchoring
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle
import it.neckar.geometry.Direction
import it.neckar.geometry.Direction.Center
import it.neckar.geometry.Distance
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextService
import it.neckar.open.i18n.resolve
import it.neckar.open.unit.other.px


/**
 * Shows a text as layer in the center of the canvas
 */
class TextLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  constructor(
    lines: LinesProvider,
    additionalConfiguration: Configuration.() -> Unit = {},

    ) : this(Configuration(lines), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override var type: LayerType = LayerType.Content

  private val painter = TextPainter()

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.font(configuration.font)

    gc.saved {
      val anchorPoint = configuration.anchorPointProvider.calculateBasePoint(gc.boundingBox)
      gc.translate(anchorPoint.x, anchorPoint.y)
      if (DebugFeature.ShowBounds.enabled(paintingContext)) {
        gc.paintMark()
      }
      painter.paintText(
        gc,
        lines = configuration.linesProvider(paintingContext.chartSupport.textService, paintingContext.i18nConfiguration),
        textColor = configuration.textColor,
        boxStyle = configuration.boxStyle,
        lineSpacing = configuration.lineSpacing,
        horizontalAlignment = configuration.horizontalAlignment,
        anchorDirection = configuration.anchorDirection,
        anchorGapHorizontal = configuration.anchorGapHorizontal,
        anchorGapVertical = configuration.anchorGapVertical,
      )

      //painter.paint(
      //  gc,
      //  lines = data.messageProvider(paintingContext.chartSupport.textService, paintingContext.chartSupport.i18nSupport.textLocale),
      //  textColor = style.textColor,
      //  boxStyle = style.boxStyle,
      //  lineSpacing = style.lineSpacing,
      //  horizontalAlignment = style.horizontalAlignment,
      //  anchorDirection = style.anchorDirection,
      //  anchorGap = style.anchorGap,
      //  insets = style.margin
      //)
    }
  }

  @ConfigurationDsl
  class Configuration(
    var linesProvider: LinesProvider,
  ) {
    /**
     * The color of the text
     */
    var textColor: Color = Color.black

    /**
     * The style for the box (background fill + border stroke)
     */
    var boxStyle: BoxStyle = BoxStyle.none

    /**
     * Describes the font
     */
    var font: FontDescriptorFragment = FontDescriptorFragment(26.0)

    /**
     * The line spacing
     */
    var lineSpacing: LineSpacing = LineSpacing.Single

    /**
     * The base point provider that is used to calculate the base point
     */
    var anchorPointProvider: BasePointProvider = DirectionBasedBasePointProvider(Center)

    fun topLeft(distance: Distance = Distance.of(20.0, 20.0)) {
      anchorDirection = Direction.TopLeft
      anchorPointProvider = DirectionBasedBasePointProvider(Direction.TopLeft, distance)
    }

    /**
     * The anchor direction - describes where the text is painted relative to the base point
     */
    var anchorDirection: Direction = Center

    /**
     * The gap for
     */
    var anchorGapHorizontal: Double = 0.0
    var anchorGapVertical: Double = 0.0

    /**
     * The alignment of the text (within the box)
     * Only relevant for multi line text
     */
    var horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Center

    /**
     * The distance between the box and the anchor point.
     * Not relevant when the anchor is [Center]
     */
    var margin: Insets = Insets.empty

  }

  companion object {
    /**
     * A text layer that prints a hello message
     */
    val helloMeisterChart: TextLayer = forText({ _, _ -> "Hello MeisterCharts" })

    fun forText(text: TextProvider, styleConfiguration: Configuration.() -> Unit = {}): TextLayer {
      return TextLayer(text.asLinesProvider(), styleConfiguration)
    }

    /**
     * Creates a new text layer that has no text
     */
    fun empty(styleConfiguration: Configuration.() -> Unit = {}): TextLayer {
      return TextLayer(lines = { _, _ -> emptyList() }, styleConfiguration)
    }
  }
}

/**
 * Adds a text layer with a text that is resolved
 */
fun Layers.addText(textKey: TextKey, color: Color = Color.blueviolet): TextLayer {
  return addText(listOf(textKey), color)
}

/**
 * Adds a text layer with texts that are resolved
 */
fun Layers.addText(textKeys: List<TextKey>, color: Color = Color.blueviolet): TextLayer {
  return addText(textKeys) {
    textColor = color
  }
}

/**
 * Adds a text layer with a text that is not resolved
 */
fun Layers.addTextUnresolved(text: String, color: Color = Color.blueviolet): TextLayer {
  return addTextUnresolved(listOf(text), color)
}

/**
 * Adds a message layer with texts that are not resolved
 */
fun Layers.addTextUnresolved(texts: List<String>, color: Color = Color.blueviolet): TextLayer {
  return addTextUnresolved(texts) {
    textColor = color
  }
}

/**
 * Adds a text layer with a text that is resolved
 */
fun Layers.addText(textKey: TextKey, styleConfiguration: TextLayer.Configuration.() -> Unit): TextLayer {
  return addText(listOf(textKey), styleConfiguration)
}

/**
 * Adds a text layer with texts that are resolved
 */
fun Layers.addText(textKeys: List<TextKey>, styleConfiguration: TextLayer.Configuration.() -> Unit): TextLayer {
  return addText(textKeys.asLinesProvider(), styleConfiguration)
}

/**
 * Adds a message layer with a fixed string that is not resolved.
 */
fun Layers.addTextUnresolved(texts: String, styleConfiguration: TextLayer.Configuration.() -> Unit): TextLayer {
  return addTextUnresolved(listOf(texts), styleConfiguration)
}

/**
 * Adds a message layer with fixed strings that are not resolved.
 */
fun Layers.addTextUnresolved(lines: List<String>, styleConfiguration: TextLayer.Configuration.() -> Unit): TextLayer {
  return addText({ _, _ -> lines }, styleConfiguration)
}


/**
 * Adds a message layer
 */
fun Layers.addText(
  linesProvider: LinesProvider,
  styleConfiguration: TextLayer.Configuration.() -> Unit = {},
): TextLayer {
  return TextLayer(linesProvider, styleConfiguration)
    .also {
      addLayer(it)
    }
}

/**
 * Adds a text layer containing multiple lines
 */
fun Layers.addTexts(
  linesProvider: LinesProvider,
): TextLayer {
  return TextLayer(linesProvider) { }
    .also {
      addLayer(it)
    }
}

/**
 * Adds a single line text layer
 */
fun Layers.addText(
  textprovider: TextProvider,
): TextLayer {
  return TextLayer.forText(textprovider) { }
    .also {
      addLayer(it)
    }
}

/**
 * Paints the text
 */
class TextPainter {
  fun paintText(
    gc: CanvasRenderingContext,
    lines: List<String>,

    textColor: Color,
    boxStyle: BoxStyle,

    lineSpacing: LineSpacing,
    horizontalAlignment: HorizontalAlignment,

    anchoring: Anchoring,

    /**
     * The max string width
     */
    maxStringWidth: Double = Double.MAX_VALUE,
    /**
     * A callback that adjusts the size of the text-box
     */
    textBoxSizeAdjustment: ((textBox: @px Rectangle, gc: CanvasRenderingContext) -> Size)? = null,
  ) {
    gc.translate(anchoring.anchor.x, anchoring.anchor.y)
    gc.paintTextBox(
      lines,
      lineSpacing,
      horizontalAlignment,
      anchoring.anchorDirection,
      anchoring.gapHorizontal,
      anchoring.gapVertical,
      boxStyle,
      textColor,
      maxStringWidth,
      textBoxSizeAdjustment
    )
  }

  /**
   * Paints the message at 0/0
   */
  fun paintText(
    gc: CanvasRenderingContext,
    lines: List<String>,

    textColor: Color,
    boxStyle: BoxStyle,

    lineSpacing: LineSpacing,
    horizontalAlignment: HorizontalAlignment,

    anchorDirection: Direction,
    anchorGapHorizontal: @px Double = 0.0,
    anchorGapVertical: @px Double = 0.0,

    /**
     * The max string width
     */
    maxStringWidth: Double = Double.MAX_VALUE,
    /**
     * A callback that adjusts the size of the text-box
     */
    textBoxSizeAdjustment: ((textBox: @px Rectangle, gc: CanvasRenderingContext) -> Size)? = null,
  ) {
    gc.paintTextBox(
      lines,
      lineSpacing,
      horizontalAlignment,
      anchorDirection,
      anchorGapHorizontal,
      anchorGapVertical,
      boxStyle,
      textColor,
      maxStringWidth,
      textBoxSizeAdjustment
    )
  }
}

/**
 * Returns the resolved text keys
 */
private fun List<TextKey>.asLinesProvider(): LinesProvider {
  return { textService: TextService, i18nSupport ->
    map {
      it.resolve(textService, i18nSupport)
    }
  }
}
