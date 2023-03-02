package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.layout.cache.DoubleCache
import com.meistercharts.canvas.saved
import com.meistercharts.model.Side
import it.neckar.open.kotlin.lang.checkEquals
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.fastForEachIndexed

/**
 * Lays out [ValueAxisLayer]s from left to right and paints them.
 *
 * ATTENTION: This layer uses [ValueAxisLayer.Style.margin] to place the layers.
 *
 *
 * Use [MultipleLayersDelegatingLayer] to place the corresponding [AxisTopTopTitleLayer]
 */
class MultiValueAxisLayer constructor(
  valueAxesProvider: ValueAxesProvider,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  val configuration: Configuration = Configuration(valueAxesProvider).also(additionalConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paintingVariables(): ValueAxesPaintingVariables {
    return paintingVariables
  }

  /**
   * The painting variables that have been calculated
   */
  private val paintingVariables = object : ValueAxesPaintingVariables {
    /**
     * The x-coordinates of the visible value axes.
     *
     * ATTENTION: The size of the cache corresponds to the number of value axis.
     * If there isn't enough space it is possible that less axis are visible
     *
     * Use [visibleAxisCount] to determine the number of visible axis
     */
    override val locationsX: @Window DoubleCache = DoubleCache()

    /**
     * The number of visible axis - that have enough space!
     */
    override var visibleAxisCount = 0

    /**
     * The total width used for the visible value axes (without trailing gap)
     */
    override var totalWidth: @Zoomed Double = 0.0

    override fun calculate(paintingContext: LayerPaintingContext) {
      val availableWidth = paintingContext.width * configuration.valueAxesMaxWidthPercentage

      val size = configuration.valueAxesProvider.size()

      //Reset all variables
      visibleAxisCount = 0
      locationsX.reset()
      locationsX.ensureSize(size)
      totalWidth = 0.0
      //The current left side (start) of the *next* axis
      var currentX = 0.0

      configuration.valueAxesProvider.fastForEachIndexed { index, valueAxisLayer ->
        checkEquals(valueAxisLayer.style.side, Side.Left) { "only left side is supported" }

        //Is there enough room left for this axis?
        if (currentX + valueAxisLayer.style.size > availableWidth) {
          //Not enough room, stop adding axes
          return
        }

        //The axis has room, will be added
        visibleAxisCount++
        locationsX[index] = currentX //Remember the location

        //Update the margin accordingly
        valueAxisLayer.style.margin = valueAxisLayer.style.margin.withLeft(currentX)
        //Calculate the layout for the current value axis layer
        paintingContext.gc.saved {
          valueAxisLayer.layout(paintingContext)
        }

        //Update the variables for the next axis
        currentX += valueAxisLayer.style.size
        totalWidth = currentX
        currentX += configuration.valueAxesGap
      }
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    //paint the background first
    configuration.background()?.let { background ->
      gc.fill(background)
      gc.fillRect(0.0, 0.0, paintingVariables.totalWidth, gc.height)
    }

    paintingVariables.visibleAxisCount.fastFor { axisIndex ->
      gc.saved {
        val valueAxisLayer = configuration.valueAxesProvider.valueAt(axisIndex)
        valueAxisLayer.paint(paintingContext)
      }
    }
  }

  @StyleDsl
  class Configuration(
    /**
     * Provides the value axes
     */
    val valueAxesProvider: ValueAxesProvider,
  ) {
    /**
     * The background color - if any
     */
    var background: () -> Color? = { null }

    /**
     * The maximum available width for the value axis layers relative to the window width
     */
    var valueAxesMaxWidthPercentage: @WindowRelative Double = 0.6

    /**
     * The gap between each value axes
     */
    var valueAxesGap: @Zoomed Double = 0.0
  }
}

/**
 * Provides the value axes (plural)
 */
typealias ValueAxesProvider = SizedProvider<ValueAxisLayer>

interface ValueAxesPaintingVariables : PaintingVariables {
  /**
   * The x-coordinates of the visible value axes
   */
  val locationsX: @Window DoubleCache

  /**
   * The total width used for the visible value axes
   */
  val totalWidth: @Zoomed Double

  /**
   * The number of visible axis - that have enough space!
   */
  val visibleAxisCount: Int
}
