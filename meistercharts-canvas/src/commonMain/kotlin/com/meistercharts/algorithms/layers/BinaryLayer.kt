package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.BinaryValueRange
import com.meistercharts.algorithms.painter.BinaryPainter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.StyleDsl
import it.neckar.open.provider.BooleanValuesProvider

/**
 * Paints a binary curve (0..1)
 */
class BinaryLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  val style: Style = Style().also(styleConfiguration)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    //Translate to the origin of the content area
    gc.translate(chartCalculator.contentAreaRelative2windowX(0.0), chartCalculator.contentAreaRelative2windowY(0.0))

    val baseLine = chartCalculator.domainRelative2zoomedY(0.0)

    val maxHeight = chartCalculator.contentAreaRelative2zoomedY(1.0)
    val maxWidth = chartCalculator.contentAreaRelative2zoomedX(1.0)

    val binaryPainter = BinaryPainter(false, false, baseLine, maxWidth, maxHeight).also {
      it.lineWidth = style.lineWidth
      it.stroke = style.stroke
      it.shadow = style.shadow
      it.areaFill = style.areaFill
      it.shadowOffsetX = style.shadowOffset
      it.shadowOffsetY = style.shadowOffset
    }

    for (i in 0 until data.valuesProvider.size()) {
      val value = data.valuesProvider.valueAt(i)

      @DomainRelative val domainRelativeY = BinaryValueRange.toDomainRelative(value)
      val y = chartCalculator.domainRelative2zoomedY(domainRelativeY)

      binaryPainter.addCoordinate(gc, chartCalculator.domainRelative2zoomedX(0.1 * i), y)
    }

    binaryPainter.finish(gc)
  }

  class Data(
    val valuesProvider: BooleanValuesProvider
  )

  @StyleDsl
  class Style {
    var lineWidth: Double = 5.0
    var stroke: Color = Color.rgba(10, 10, 10, 0.5)
    var shadow: Color? = null
    var areaFill: Color? = null
    var shadowOffset: Double = 4.0
  }

}
