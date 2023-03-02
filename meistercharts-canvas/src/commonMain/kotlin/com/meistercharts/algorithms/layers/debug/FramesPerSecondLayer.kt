package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import it.neckar.open.unit.other.fps
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import kotlin.math.roundToInt

/**
 * Displays the frames painted per second.
 */
class FramesPerSecondLayer(
  /**
   * The update rate in milliseconds (how much time must be passed before the label showing the FPS is updated)
   */
  @ms
  val updateRate: Double = 500.0,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Notification

  /**
   * The current value for frames per second
   */
  @fps
  private var framesPerSecond = 0

  /**
   * The timestamp when the layer has been updated the last time
   */
  @ms
  private var lastUpdatedTimestamp = 0.0

  override fun paint(paintingContext: LayerPaintingContext) {
    //Only update the FPS every [updateRate] milli seconds
    if (paintingContext.frameTimestamp - lastUpdatedTimestamp > updateRate) {
      framesPerSecond = paintingContext.layerSupport.paintStatisticsSupport.fps.roundToInt()
      lastUpdatedTimestamp = paintingContext.frameTimestamp
    }

    val text = "FPS: $framesPerSecond"

    @px val x = 10.0 // TODO must be positioned from caller
    @px val y = 10.0 // TODO must be positioned from caller

    val gc = paintingContext.gc
    gc.font(style.font)
    @px val width = gc.calculateTextWidth(text) + style.padding.offsetWidth
    @px val height = gc.getFontMetrics().totalHeight + style.padding.offsetHeight

    gc.fillStyle(style.background)
    gc.fillRect(x, y, width, height)

    gc.font(style.font)
    gc.fillStyle(style.text)

    gc.fillText(text, x + style.padding.left, y + style.padding.top, Direction.TopLeft)
  }

  @StyleDsl
  open class Style {
    /**
     * The color to be used for the background
     */
    val background: Color = Color.web("rgba(255, 255, 255, 0.7)")

    /**
     * The color to be used for the text
     */
    val text: Color = Color.web("#888888")

    /**
     * The font to be used for the text
     */
    var font: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * The padding to be used
     */
    val padding: Insets = Insets.of(3.0, 5.0)

  }
}

/**
 * Adds a [FramesPerSecondLayer] to this [Layers]
 */
fun Layers.addFramesPerSecond() {
  addLayer(FramesPerSecondLayer())
}
