package it.neckar.open.kotlin.lang

import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.other.pct

/**
 * Extension function on the [Double] type that performs linear interpolation
 * between two values [start] and [end] based on the receiver [Double] value
 * acting as the interpolation parameter.
 *
 * @receiver The interpolation parameter (percentage), a value between 0 and 1.
 *           When the receiver is 0, the result is equal to [start].
 *           When the receiver is 1, the result is equal to [end].
 *           When the receiver is between 0 and 1, the result is a weighted
 *           average of [start] and [end].
 * @param[start] The first value to interpolate from.
 * @param[end] The second value to interpolate to.
 * @return The linearly interpolated value between [start] and [end] based on
 *         the receiver [Double] value as the interpolation parameter.
 */
fun @pct Double.interpolate(start: @Inclusive Double, end: @Inclusive Double): Double = (start + (end - start) * this)

/**
 * Returns the interpolated value between start and end values.
 * This is interpreted as percentage.
 *
 * E.g: `0.5.interpolate(40.0, 60.0)` returns `50.0`
 */
fun Double.interpolate(start: Int, end: Int): Int = (start + (end - start) * this).toInt()

/**
 * Returns in percentage the distance of the value between lowerBound and upperBound
 *
 * E.g: returns 0.5 for 20.0.relativeDistanceBetween(40.0, 80.0)
 * E.g: returns -3.5 for 10.0.relativeDistanceBetween(80.0, 100.0)
 */
fun Double.relativeDistanceBetween(lowerBound: Double, upperBound: Double): @pct Double {
  val delta = upperBound - lowerBound
  require(delta != 0.0) { "lowerBound ($lowerBound) must be different than upperBound ($upperBound)" }

  val relativeThis = this - lowerBound
  return relativeThis / delta
}
