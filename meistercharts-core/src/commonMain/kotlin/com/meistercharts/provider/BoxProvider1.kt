package com.meistercharts.provider

import it.neckar.open.unit.number.MayBeNegative

/**
 * Provides box values with one parameter
 */
interface BoxProvider1<T> {
  @MayBeNegative
  fun getX(param0: T): Double

  @MayBeNegative
  fun getY(param0: T): Double

  @MayBeNegative
  fun getWidth(param0: T): Double

  @MayBeNegative
  fun getHeight(param0: T): Double
}
