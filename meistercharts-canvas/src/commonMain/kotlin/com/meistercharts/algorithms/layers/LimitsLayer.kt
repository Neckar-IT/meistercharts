package com.meistercharts.algorithms.layers

import com.meistercharts.model.Orientation
import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.painter.AreaPainter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.model.SidesSelection
import com.meistercharts.provider.LimitsProvider
import it.neckar.open.provider.fastForEach
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmOverloads

/**
 * Visualizes limits (as area with a border) horizontally or vertically
 *
 */
class LimitsLayer @JvmOverloads constructor(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  private val areaPainter = AreaPainter(snapXValues = false, snapYValues = false)

  override fun paint(paintingContext: LayerPaintingContext) {
    areaPainter.apply {
      fill = style.fill
      borderColor = style.stroke
      borderWidth = style.strokeWidth
    }

    data.limitsProvider.fastForEach {
      when (style.orientation) {
        Orientation.Vertical -> {
          paintVertical(paintingContext, it, areaPainter)
        }
        Orientation.Horizontal -> {
          paintHorizontal(paintingContext, it, areaPainter)
        }
      }
    }
  }

  /**
   * Limits have horizontal borders:
   * ```
   * XXXXXXX
   * ━━━━━━━
   *
   * ━━━━━━━
   * XXXXXXX
   *```
   */
  private fun paintVertical(paintingContext: LayerPaintingContext, limit: Limit, areaPainter: AreaPainter) {
    val gc = paintingContext.gc
    @Window val limitWindow = paintingContext.chartCalculator.domainRelative2windowY(limit.limit)

    fun paintLimitAtBottom() {
      areaPainter.borderSides = SidesSelection.onlyTop
      areaPainter.paintArea(gc, 0.0, limitWindow, gc.width, gc.height)
    }

    fun paintLimitAtTop() {
      areaPainter.borderSides = SidesSelection.onlyBottom
      areaPainter.paintArea(gc, 0.0, 0.0, gc.width, limitWindow)
    }

    when (limit) {
      is UpperLimit -> {
        when (paintingContext.chartState.axisOrientationY) {
          AxisOrientationY.OriginAtBottom -> paintLimitAtTop()
          AxisOrientationY.OriginAtTop -> paintLimitAtBottom()
        }
      }
      is LowerLimit -> {
        when (paintingContext.chartState.axisOrientationY) {
          AxisOrientationY.OriginAtBottom -> paintLimitAtBottom()
          AxisOrientationY.OriginAtTop -> paintLimitAtTop()
        }
      }
    }
  }


  /**
   * Limits have vertical borders:
   * ```
   *  X┃   ┃X
   *  X┃   ┃X
   *  X┃   ┃X
   *```
   */
  private fun paintHorizontal(paintingContext: LayerPaintingContext, limit: Limit, areaPainter: AreaPainter) {
    val gc = paintingContext.gc
    @Window val limitWindow = paintingContext.chartCalculator.domainRelative2windowX(limit.limit)

    fun paintLimitAtLeft() {
      areaPainter.borderSides = SidesSelection.onlyRight
      areaPainter.paintArea(gc, 0.0, 0.0, limitWindow, gc.height)
    }

    fun paintLimitAtRight() {
      areaPainter.borderSides = SidesSelection.onlyLeft
      areaPainter.paintArea(gc, limitWindow, 0.0, gc.width, gc.height)
    }

    when (limit) {
      is LowerLimit -> {
        when (paintingContext.chartState.axisOrientationX) {
          AxisOrientationX.OriginAtLeft -> paintLimitAtLeft()
          AxisOrientationX.OriginAtRight -> paintLimitAtRight()
        }
      }
      is UpperLimit -> {
        when (paintingContext.chartState.axisOrientationX) {
          AxisOrientationX.OriginAtLeft -> paintLimitAtRight()
          AxisOrientationX.OriginAtRight -> paintLimitAtLeft()
        }
      }
    }
  }

  class Data(
    /**
     * Provides the limits
     */
    val limitsProvider: LimitsProvider
  )

  @StyleDsl
  open class Style {
    /**
     * The color for the lines
     */
    var stroke: Color? = Color.gray

    /**
     * The width of the stroke
     */
    var strokeWidth: @px Double = 1.0

    /**
     * The color for the area
     */
    var fill: Color? = Color.silver

    /**
     * The orientation of this layer:
     *  * [Orientation.Vertical]: the limits are drawn horizontally
     *  * [Orientation.Horizontal]: the limits are drawn vertically
     */
    var orientation: Orientation = Orientation.Vertical
  }
}

/**
 * Describes limits
 */
sealed class Limit(@DomainRelative val limit: Double)

/**
 * An upper limit
 */
class UpperLimit(@DomainRelative limit: Double) : Limit(limit)

/**
 * A lower limit
 */
class LowerLimit(@DomainRelative limit: Double) : Limit(limit)
