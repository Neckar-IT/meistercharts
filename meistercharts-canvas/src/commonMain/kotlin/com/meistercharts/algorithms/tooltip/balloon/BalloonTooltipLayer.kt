package com.meistercharts.algorithms.tooltip.balloon

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PaintingVariables
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.layout.cache.CoordinatesCache
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import it.neckar.open.provider.CoordinatesProvider1
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.provider.fastForEachIndexed

/**
 * Layer that shows a balloon tooltip (if configured)
 */
class BalloonTooltipLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  init {
    configuration.also(additionalConfiguration)
  }

  override val type: LayerType = LayerType.Notification

  /**
   * The tooltip painter that is used to paint the tooltips
   */
  val tooltipPainter: BalloonTooltipPaintable = BalloonTooltipPaintable(Paintable.NoOp) //the content will be set later

  override fun paintingVariables(): PaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : PaintingVariables {
    /**
     * Contains the coordinates where the balloon tooltip is placed
     */
    @Window
    val coordinates = CoordinatesCache()

    override fun calculate(paintingContext: LayerPaintingContext) {
      val size = configuration.coordinates.size(paintingContext)
      coordinates.ensureSize(size)

      configuration.coordinates.fastForEachIndexed(paintingContext) { index, x, y ->
        coordinates.set(index, x, y)

        val contentPainter = configuration.tooltipContent.valueAt(index, paintingContext)
        tooltipPainter.configuration.content = contentPainter
        tooltipPainter.layout(paintingContext, contentPainter)
      }
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    paintingVariables.coordinates.fastForEachIndexed { _, x, y ->
      gc.saved {
        gc.translate(x, y)
        tooltipPainter.paint(paintingContext)
      }
    }
  }

  /**
   * Identifies a tooltip by index. Used for the multi providers
   */
  @Target(AnnotationTarget.TYPE)
  annotation class TooltipIndex

  class Configuration(
    /**
     * Provides the coordinates for the balloon tooltip
     */
    var coordinates: @TooltipIndex @Window CoordinatesProvider1<LayerPaintingContext> = CoordinatesProvider1.Empty,

    /**
     * Provides the tooltip painter
     */
    var tooltipContent: MultiProvider1<TooltipIndex, Paintable, LayerPaintingContext>,
  )
}


