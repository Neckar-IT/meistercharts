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
