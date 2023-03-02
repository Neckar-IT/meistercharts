package com.meistercharts.algorithms

import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import it.neckar.open.unit.number.MayBeNaN

/**
 * Represents a linear value range that has a start and end.
 * All values are calculated by interpolating
 */
open class LinearValueRange(
  start: @Domain Double,
  end: @Domain Double
) : ValueRange(start, end) {

  /**
   * Converts a domain value to domain relative.
   *
   * ATTENTION: Only works for *absolute* domain values.
   * For deltas between two domain values use [deltaToDomainRelative] instead.
   *
   * ATTENTION: Will return Nan/Infinity/-Infinity if [delta] of this value range is 0.0
   */
  override fun toDomainRelative(@Domain domainValue: Double): @DomainRelative @MayBeNaN Double {
    return (domainValue - start) / delta
  }

  /**
   * Converts a delta to a domain relative delta
   * ATTENTION: Will return Nan/Infinity/-Infinity if [delta] of this value range is 0.0
   */
  fun deltaToDomainRelative(@Domain domainDelta: Double): @DomainRelative @MayBeNaN Double {
    return domainDelta / delta
  }

  override fun toDomain(@DomainRelative domainRelative: Double): @Domain Double {
    return domainRelative * delta + start
  }

  /**
   * Converts a delta from domain relative to domain
   */
  fun deltaToDomain(@DomainRelative domainRelative: Double): @Domain Double {
    return domainRelative * delta
  }

  fun withStart(newStart: @Domain Double): LinearValueRange {
    return LinearValueRange(newStart, end)
  }

  fun withEnd(newEnd: @Domain Double): LinearValueRange {
    return LinearValueRange(start, newEnd)
  }
}
