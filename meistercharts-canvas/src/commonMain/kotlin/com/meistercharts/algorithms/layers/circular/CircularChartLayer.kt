package com.meistercharts.algorithms.layers.circular

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import kotlin.math.min

/**
 * A circular chart layer
 */
class CircularChartLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  constructor(
    valuesProvider: @pct DoublesProvider,
    styleConfiguration: Style.() -> Unit = {}
  ) : this(Data(valuesProvider), styleConfiguration)

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val circularChartPaintable = CircularChartPaintable(data.valuesProvider, style)

    with(paintingContext.chartCalculator) {
      //The center of the domain relative area
      @Window val center = contentAreaRelative2window(0.5, 0.5)

      //The size of the outer circle - describes the *outer* side of the line
      @Zoomed val outerRadius = contentAreaRelative2zoomed(0.5, 0.5)
        .let {
          //take the smaller size
          min(it.width, it.height).coerceAtMost(style.maxDiameter / 2.0)
        }

      //Paint the donut using a painter
      circularChartPaintable.outerLineRadiusOutside = outerRadius
      circularChartPaintable.style.outerCircleWidth
      circularChartPaintable.paint(paintingContext, center)
    }
  }

  class Data(
    /**
     * Provides values for circular chart segments.
     * The values are provided in percentage. The sum of the values must not be greater than 1.0
     */
    val valuesProvider: @pct DoublesProvider
  )

  open class Style : CircularChartPaintable.Style() {
    /**
     * The maximum size of the circle (outer width)
     */
    var maxDiameter: @px Double = 500.0
  }
}

