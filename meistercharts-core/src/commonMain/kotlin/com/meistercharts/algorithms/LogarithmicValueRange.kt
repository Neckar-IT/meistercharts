package com.meistercharts.algorithms

import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import it.neckar.open.unit.number.Positive
import kotlin.math.log10
import kotlin.math.pow

/**
 * A logarithmic value range
 */
class LogarithmicValueRange(
  start: @Domain @Positive Double,
  end: @Domain @Positive Double
) : ValueRange(start, end) {

  init {
    require(start > 0.0) { "start must be greater than 0 but was <$start>" }
    require(end > 0.0) { "end must be greater than 0 but was <$end>" }
  }

  /**
   * log10 of the domain value of start
   */
  val logStart: Double = log10(start)

  /**
   * log10 of the domain value of end
   */
  val logEnd: Double = log10(end)

  /**
   * Log of the delta between end and start
   */
  val logDelta: Double = logEnd - logStart

  override fun toDomainRelative(domainValue: @Domain Double): @DomainRelative Double {
    if (domainValue <= 0.0) {
      //values less than or equal to 0 are not supported -> fall back to 0 percent of the domain
      return 0.0
    }
    val delta = log10(domainValue) - logStart
    return delta / logDelta
  }

  override fun toDomain(domainRelative: @DomainRelative Double): @Domain Double {
    val logValue = domainRelative * logDelta + logStart
    return 10.0.pow(logValue)
  }

}
