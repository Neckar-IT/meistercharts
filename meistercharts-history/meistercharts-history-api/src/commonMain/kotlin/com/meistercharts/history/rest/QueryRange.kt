package com.meistercharts.history.rest

import com.meistercharts.history.SamplingPeriod
import it.neckar.open.unit.si.ms

/**
 * Describes the time range that is queried
 */
data class QueryRange(
  val from: @ms Double,
  val to: @ms Double,
  val resolution: SamplingPeriod
) {
}
