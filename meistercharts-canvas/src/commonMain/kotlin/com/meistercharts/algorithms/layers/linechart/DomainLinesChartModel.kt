/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
