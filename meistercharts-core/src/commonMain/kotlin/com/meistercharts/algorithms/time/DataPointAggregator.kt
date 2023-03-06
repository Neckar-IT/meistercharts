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
package com.meistercharts.algorithms.time

import com.meistercharts.algorithms.aggregate.AggregatedValue
import it.neckar.open.unit.si.ms

/**
 * Aggregates data points
 *
 */
class DataPointAggregator {
  /**
   * Aggregates the given data points
   *
   * @param dataPoints the data points
   */
  fun aggregate(dataPoints: List<DataPoint<Double>>, @ms timeSpan: Double): List<AggregatedValue> {
    val builder: MutableList<AggregatedValue> = mutableListOf()

    val aggregatedValueBuilder = AggregatedValue.Builder(timeSpan)

    for (dataPoint in dataPoints) {
      if (!aggregatedValueBuilder.fits(dataPoint.time)) {
        //Do no longer fit, create a new one
        builder.add(aggregatedValueBuilder.build())
        aggregatedValueBuilder.clear()
      }

      aggregatedValueBuilder.add(dataPoint.time, dataPoint.value)
    }

    //Add the last one
    if (!aggregatedValueBuilder.isEmpty) {
      builder.add(aggregatedValueBuilder.build())
    }

    return builder
  }
}
