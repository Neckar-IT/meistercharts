package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.LinearValueRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.provider.DefaultDoublesProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.other.px

/**
 * Paints a single bar with a label - with a configurable total height
 */
class StackedBarWithLabelPaintable(
  val data: Data = Data(),
  width: @px Double = 15.0,
  height: @px Double = 200.0
) : Paintable {

  constructor(
    name: String = "",
    valuesProvider: DoublesProvider = DefaultDoublesProvider(listOf(5.0, 6.0, 7.0)),
    valueRange: LinearValueRange = ValueRange.default,
    colors: List<Color>,
    width: @px Double = 15.0,
    height: @px Double = 200.0
  ) : this(Data(name, valuesProvider, valueRange), width, height) {
    stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(colors, Color.gray)
  }

  val style: Style = Style()

  val stackedBarPaintable: StackedBarPaintable = StackedBarPaintable(StackedBarPaintable.Data(data.valuesProvider, data.valueRange), width, height)

  var width: Double by stackedBarPaintable::width
  var height: Double by stackedBarPaintable::height

  init {
    this.width = width
    this.height = height
  }

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle = Rectangle(Coordinates.origin, Size(stackedBarPaintable.width, stackedBarPaintable.height))

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc

    gc.translate(x, y)

    gc.saved {
      stackedBarPaintable.paint(paintingContext, 0.0, 0.0)
    }

    gc.font(FontDescriptorFragment.L)
    gc.paintTextBox(line = data.name, anchorDirection = Direction.TopCenter, anchorGapHorizontal = 5.0, anchorGapVertical = 5.0, boxStyle = style.labelBoxStyle, textColor = style.labelColor)
  }

  class Data(
    var name: String = "",
    var valuesProvider: DoublesProvider = DefaultDoublesProvider(listOf(5.0, 6.0, 7.0)),
    var valueRange: LinearValueRange = ValueRange.default
  )

  @StyleDsl
  class Style {
    /**
     * The color of the label text
     */
    var labelColor: Color = Color.web("#373e44")

    /**
     * The box style for the label
     */
    var labelBoxStyle: BoxStyle = BoxStyle(
      fill = Color("rgba(255, 255, 255, 0.55)"),
      padding = Insets(3.0, 5.0, 3.0, 5.0)
    )
  }
}
