package com.meistercharts.fx.axis

import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.axis.IntermediateValuesMode
import com.meistercharts.algorithms.axis.LinearAxisTickCalculator.calculateTickValues
import it.neckar.open.kotlin.lang.isNanOrInfinite
import it.neckar.open.javafx.properties.*
import it.neckar.open.unit.other.px
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Side
import javafx.scene.chart.ValueAxis
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.floor

/**
 * An axis that shows only the rounded numbers
 */
class RoundedNumberAxis
@JvmOverloads constructor(
  lower: Double = 0.0,
  upper: Double = 100.0,
  @px val horizontalTickSpace: Double = 50.0,
  @px val verticalTickSpace: Double = 25.0

) : ValueAxis<Number>(lower, upper) {

  val tickLabelFormatProperty: ObjectProperty<NumberFormat> = SimpleObjectProperty<NumberFormat>(DecimalFormat())
  var tickLabelFormat: NumberFormat by tickLabelFormatProperty

  init {
    isAutoRanging = false
  }

  override fun getRange(): Any? {
    return null
  }

  override fun setRange(range: Any?, animate: Boolean) {
  }

  override fun getTickMarkLabel(value: Number?): String {
    if (value == null) {
      return ""
    }

    return tickLabelFormat.format(value)
  }

  override fun calculateTickValues(length: Double, range: Any?): List<Number> {
    if (lowerBound > upperBound || lowerBound.isNanOrInfinite() || upperBound.isNanOrInfinite()) {
      return emptyList()
    }
    val tickCount = calculateTickCount(width, height, side)
    return calculateTickValues(lowerBound, upperBound, AxisEndConfiguration.Exact, tickCount, 0.0, IntermediateValuesMode.Also5and2).asList()
  }

  override fun calculateMinorTickMarks(): MutableList<Number> {
    return mutableListOf()
  }

  private fun calculateTickCount(@px width: Double, @px height: Double, side: Side): Int {
    if (side.isHorizontal) {
      return floor(width / horizontalTickSpace).toInt()
    }

    if (side.isVertical) {
      return floor(height / verticalTickSpace).toInt()
    }

    throw IllegalArgumentException("Invalid side <$side>")
  }
}
