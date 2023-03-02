package com.meistercharts.algorithms.layers.linechart

import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.provider.TimeRangeProvider
import com.meistercharts.provider.ValueRangeProvider

/**
 * A lines chart model that uses values ranges to convert @Domain to @DomainRelative
 */
@DomainRelative
class DomainLinesChartModel(
  /**
   * The charts model that returns *@Domain* values
   */
  val delegate: @Domain LinesChartModel,
  val valueRangeProviderX: ValueRangeProvider?,
  val valueRangeProviderY: ValueRangeProvider?
) : LinesChartModel {

  override val linesCount: Int
    get() = delegate.linesCount


  override fun pointsCount(lineIndex: Int): Int {
    return delegate.pointsCount(lineIndex)
  }

  override fun valueX(lineIndex: Int, index: Int): Double {
    @Domain @DomainRelative val x = delegate.valueX(lineIndex, index)

    return valueRangeProviderX?.let {
      return it().toDomainRelative(x)
    } ?: x
  }

  override fun valueY(lineIndex: Int, index: Int): Double {
    @Domain @DomainRelative val y = delegate.valueY(lineIndex, index)

    return valueRangeProviderY?.let {
      it().toDomainRelative(y)
    } ?: y
  }
}


@DomainRelative
class DomainTimeLinesChartModel(
  /**
   * The charts model that returns *@Domain* values
   */
  val delegate: @Domain LinesChartModel,
  val valueRangeProviderX: TimeRangeProvider,
  val valueRangeProviderY: ValueRangeProvider
) : LinesChartModel {

  override val linesCount: Int
    get() = delegate.linesCount


  override fun pointsCount(lineIndex: Int): Int {
    return delegate.pointsCount(lineIndex)
  }

  override fun valueX(lineIndex: Int, index: Int): Double {
    @Domain val x = delegate.valueX(lineIndex, index)
    return valueRangeProviderX().time2relative(x)
  }

  override fun valueY(lineIndex: Int, index: Int): Double {
    @Domain val y = delegate.valueY(lineIndex, index)
    return valueRangeProviderY().toDomainRelative(y)
  }
}

/**
 * Converts the lines chart model to domain relative
 */
fun @Domain LinesChartModel.toDomainRelativeY(valueRangeProviderY: ValueRangeProvider): LinesChartModel {
  return DomainLinesChartModel(this, null, valueRangeProviderY)
}
