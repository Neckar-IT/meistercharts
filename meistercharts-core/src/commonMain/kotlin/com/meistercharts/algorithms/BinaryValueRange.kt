package com.meistercharts.algorithms

import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative

/**
 * Special class that offers additional methods for binary value range (0..1)
 *
 */
object BinaryValueRange : LinearValueRange(0.0, 1.0) {
  @DomainRelative
  fun toDomainRelative(@Domain value: Boolean): Double {
    return if (value) {
      1.0
    } else 0.0
  }

  @Domain
  fun toDomainBoolean(@DomainRelative domainRelative: Double): Boolean {
    if (domainRelative == 1.0) {
      return true
    }
    if (domainRelative == 0.0) {
      return false
    }

    throw IllegalArgumentException("Invalid domain value <$domainRelative>")
  }
}
