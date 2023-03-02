package com.meistercharts.algorithms.layers.text

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.LineSpacing
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.textService
import com.meistercharts.model.Anchoring
import com.meistercharts.model.BasePointProvider
import com.meistercharts.model.Direction
import com.meistercharts.model.Direction.Center
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Insets
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextService
import it.neckar.open.i18n.resolve
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.other.px


/**
 * Shows a text as layer in the center of the canvas
 */
class TextLayer(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  constructor(
    linesProvider: LinesProvider,
    styleConfiguration: Style.() -> Unit = {}
  ) : this(Data(linesProvider), styleConfiguration)

  /**
   * The style
   */
  val style: Style = Style().apply(styleConfiguration)

  override var type: LayerType = LayerType.Content

  private val painter = TextPainter()

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.font(style.font)

    gc.saved {
      val anchorPoint = style.anchorPointProvider.calculateBasePoint(gc.boundingBox)
      gc.translate(anchorPoint.x, anchorPoint.y)
      if (DebugFeature.ShowBounds.enabled(paintingContext)) {
        gc.paintMark()
      }
      painter.paintText(
        gc,
        lines = data.linesProvider(paintingContext.chartSupport.textService, paintingContext.i18nConfiguration),
        textColor = style.textColor,
        boxStyle = style.boxStyle,
        lineSpacing = style.lineSpacing,
        horizontalAlignment = style.horizontalAlignment,
        anchorDirection = style.anchorDirection,
        anchorGapHorizontal = style.anchorGapHorizontal,
        anchorGapVertical = style.anchorGapVertical,
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

  class Data(
    var linesProvider: LinesProvider = { _, _ -> listOf("48°24'49.7\"N", "9°03'03.0\"E") }
  )

  /**
   * Style configuration for the text layer
   */
  @StyleDsl
  open class Style {
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
    val helloMeisterChart: TextLayer = TextLayer(Data { _, _ -> listOf("Hello MeisterCharts") })
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
fun Layers.addText(textKey: TextKey, styleConfiguration: TextLayer.Style.() -> Unit): TextLayer {
  return addText(listOf(textKey), styleConfiguration)
}

/**
 * Adds a text layer with texts that are resolved
 */
fun Layers.addText(textKeys: List<TextKey>, styleConfiguration: TextLayer.Style.() -> Unit): TextLayer {
  return addText(textKeys.asLinesProvider(), styleConfiguration)
}

/**
 * Adds a message layer with a fixed string that is not resolved.
 */
fun Layers.addTextUnresolved(texts: String, styleConfiguration: TextLayer.Style.() -> Unit): TextLayer {
  return addTextUnresolved(listOf(texts), styleConfiguration)
}

/**
 * Adds a message layer with fixed strings that are not resolved.
 */
fun Layers.addTextUnresolved(lines: List<String>, styleConfiguration: TextLayer.Style.() -> Unit): TextLayer {
  return addText({ _, _ -> lines }, styleConfiguration)
}


/**
 * Adds a message layer
 */
fun Layers.addText(
  linesProvider: LinesProvider,
  styleConfiguration: TextLayer.Style.() -> Unit = {}
): TextLayer {
  return TextLayer(TextLayer.Data(linesProvider), styleConfiguration)
    .also {
      addLayer(it)
    }
}

fun Layers.addText(
  linesProvider: LinesProvider,
): TextLayer {
  return TextLayer(TextLayer.Data(linesProvider)) { }
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
