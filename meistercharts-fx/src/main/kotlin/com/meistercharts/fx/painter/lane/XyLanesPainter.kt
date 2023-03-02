package com.meistercharts.fx.painter.lane

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.painter.AbstractDomainRelativePainter
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.paintMark
import it.neckar.open.unit.other.px
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import javafx.scene.paint.Color


/**
 * Paints xy columns (horizontal lines) // TODO ist das nicht ein Widerspruch?
 *
 */
@Deprecated("convert manually")
class XyLanesPainter(
  calculator: ChartCalculator,
  val domainValueRange: ValueRange,
  private val brightness2colorCache: LoadingCache<Double, com.meistercharts.algorithms.painter.Color>,
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractDomainRelativePainter(calculator, snapXValues, snapYValues) {

  /**
   * Paints one column
   *
   * @param fromX            where to paint from
   * @param toX              where to paint to
   * @param lanesInformation the lanes that are painted
   */
  fun paintLanes(gc: CanvasRenderingContext, @Window @px fromX: Double, @Window @px toX: Double, lanesInformation: LanesInformation) {
    //Paint the lanes
    for (lane in lanesInformation.lanes) {
      @Window @px val upper = calculator.domainRelative2windowY(domainValueRange.toDomainRelative(lane.upper))
      @Window @px val lower = calculator.domainRelative2windowY(domainValueRange.toDomainRelative(lane.lower))

      gc.fillStyle(brightness2colorCache.get(lane.brightness))
      fillRect(gc, snapXPosition(fromX), snapXPosition(toX + 1), snapYPosition(lower), snapYPosition(upper))
    }

    //Paint the edges
    for (edge in lanesInformation.edges) {
      @Window @px val y = calculator.domainRelative2windowY(domainValueRange.toDomainRelative(edge.position))

      gc.strokeStyle(edge.color)
      gc.strokeLine(fromX, snapYPosition(y), toX, snapXPosition(y))
    }
  }

  companion object {
    /**
     * Creates a cache
     */
    fun createCache(): LoadingCache<Double, Color> {
      return CacheBuilder.newBuilder().maximumSize(50).recordStats().build(object : CacheLoader<Double, Color>() {
        override fun load(aDouble: Double): Color {
          return Color(aDouble, aDouble, aDouble, 1.0)
        }
      })
    }
  }
}
