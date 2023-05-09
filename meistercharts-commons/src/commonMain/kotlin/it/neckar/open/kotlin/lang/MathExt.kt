package it.neckar.open.kotlin.lang

import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.other.deg
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.rad
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.*

/**
 * Math methods
 */

/**
 * Calculates the distance between two coordinates
 */
fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
  val deltaX = x1 - x2
  val deltaY = y1 - y2

  return distance(deltaX, deltaY)
}

/**
 * Returns the distance for delta x and delta y
 */
fun distance(deltaX: Double, deltaY: Double): Double {
  return (deltaX * deltaX + deltaY * deltaY).sqrt()
}

/**
 * Implementation from here: https://stackoverflow.com/questions/1560492/how-to-tell-whether-a-point-is-to-the-right-or-left-side-of-a-line
 * "Left" means that if you stand on the lineStart point and look into the direction of lineEnd, the point will be on your left side
 * If you want to know if the point is to the other side (to the "Right"), simply exchange lineStart and lineEnd
 */
fun pointIsLeftOfLine(lineStartX: Double, lineStartY: Double, lineEndX: Double, lineEndY: Double, pointX: Double, pointY: Double): Boolean {
  return (lineEndX - lineStartX) * (pointY - lineStartY) > (lineEndY - lineStartY) * (pointX - lineStartX)
}

/**
 * Converts radians to degree
 */
fun Double.toDegrees(): @deg Double {
  return this * 180.0 / PI
}

/**
 * Converts degree to radians
 */
fun Double.toRadians(): @rad Double {
  return this / 180.0 * PI
}

/**
 * Converts degree to radians
 */
fun Int.toRadians(): @rad Double {
  return this / 180.0 * PI
}

/**
 * Converts the double value to a percentage (by dividing by 100.0)
 * 1.0 -> 1% = 0.01
 */
val Double.percent: @pct Double
  get() {
    return this / 100.0
  }

/**
 * Converts the int value to a percentage (by dividing by 100.0)
 * 1 -> 1% = 0.01
 */
val Int.percent: @pct Double
  get() {
    return this / 100.0
  }

inline fun Double.isCloseTo(targetValue: Double, epsilon: Double = 0.0001): Boolean {
  return isEquivalent(this, targetValue, epsilon)
}

fun Double.isNotCloseTo(targetValue: Double, epsilon: Double = 0.0001): Boolean {
  return !isCloseTo(targetValue, epsilon)
}

/**
 * Returns true if this is close to the given value - or smaller than the given value
 */
fun Double.isCloseToOrLessThan(compareTo: @Inclusive Double, epsilon: Double = 0.0001): Boolean {
  if (this <= compareTo) {
    return true
  }

  return this.isCloseTo(compareTo, epsilon)
}

/**
 * Normalizes a value for the given base.
 * Returns the percentage of this relative to the given base
 */
inline fun Double.normalize(base: Double): @pct Double {
  return 1.0 / base * this
}

/**
 * Returns true if the double is event, false otherwise
 */
inline fun Double.isEven(): Boolean {
  return this % 2 == 0.0
}

inline fun Double.isOdd(): Boolean {
  return (this % 2).abs() == 1.0
}

fun Double.sin(): Double = kotlin.math.sin(this)
fun Double.cos(): Double = kotlin.math.cos(this)
fun Double.tan(): Double = kotlin.math.tan(this)
fun Double.asin(): Double = kotlin.math.asin(this)
fun Double.acos(): Double = kotlin.math.acos(this)
fun Double.atan(): Double = kotlin.math.atan(this)
fun Double.exp(): Double = kotlin.math.exp(this)
fun Double.log10(): Double = kotlin.math.log10(this)
fun Double.sqrt(): Double = kotlin.math.sqrt(this)
fun Double.ceil(): Double = kotlin.math.ceil(this)
fun Double.floor(): Double = kotlin.math.floor(this)
fun Double.atan2(x: Double): Double = kotlin.math.atan2(this, x)
fun Double.round(): Double = kotlin.math.round(this)
fun Double.abs(): Double = kotlin.math.abs(this)
fun Double.sinh(): Double = kotlin.math.sinh(this)
fun Double.cosh(): Double = kotlin.math.cosh(this)
fun Double.tanh(): Double = kotlin.math.tanh(this)
fun Double.expm1(): Double = kotlin.math.expm1(this)

fun Int.abs(): Int = kotlin.math.abs(this)

/**
 * Ensures that this integer value lies between 0 (inclusive) and limit (exclusive).
 *
 * If this integer value is negative it is "wrapped around" the limit.
 *
 * If this integer exceeds the limit it is "wrapped around" 0.
 */
fun Int.wrapAround(limit: Int): Int = (this % limit + limit) % limit


////////////////////
////////////////////

/** Converts this [Boolean] into integer: 1 for true, 0 for false */
inline fun Boolean.toInt() = if (this) 1 else 0

////////////////////
////////////////////

/** Converts [this] into [Int] rounding to the ceiling */
fun Float.toIntCeil(): Int = ceil(this).toInt()

/** Converts [this] into [Int] rounding to the ceiling */
fun Double.toIntCeil(): Int = ceil(this).toInt()

/** Converts [this] into [Int] rounding to the floor */
fun Float.toIntFloor(): Int = floor(this).toInt()

/** Converts [this] into [Int] rounding to the floor */
fun Double.toIntFloor(): Int = floor(this).toInt()

fun Double.toIntRound(): Int = round(this).toInt()

/** Convert this [Long] into an [Int] but throws an [IllegalArgumentException] in the case that operation would produce an overflow */
fun Long.toIntSafe(): Int = if (this in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) this.toInt() else throw IllegalArgumentException("Long doesn't fit Integer")

////////////////////
////////////////////

/** Returns an [Int] representing this [Byte] as if it was unsigned 0x00..0xFF */
inline val Byte.unsigned: Int get() = this.toInt() and 0xFF

/** Returns a [Long] representing this [Int] as if it was unsigned 0x00000000L..0xFFFFFFFFL */
inline val Int.unsigned: Long get() = this.toLong() and 0xFFFFFFFFL

////////////////////
////////////////////

/** Performs a fast integral logarithmic of base two */
fun ilog2(v: Int): Int = if (v == 0) (-1) else (31 - v.countLeadingZeros())

////////////////////
////////////////////

/** Divides [this] into [that] rounding to the floor */
infix fun Int.divFloor(that: Int): Int = this / that

/** Divides [this] into [that] rounding to the ceil */
infix fun Int.divCeil(that: Int): Int = if (this % that != 0) (this / that) + 1 else (this / that)

////////////////////
////////////////////

/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be outside the destination range */
fun Double.convertRange(srcMin: Double, srcMax: Double, dstMin: Double, dstMax: Double): Double = (dstMin + (dstMax - dstMin) * ((this - srcMin) / (srcMax - srcMin)))

/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be outside the destination range */
fun Int.convertRange(srcMin: Int, srcMax: Int, dstMin: Int, dstMax: Int): Int = (dstMin + (dstMax - dstMin) * ((this - srcMin).toDouble() / (srcMax - srcMin).toDouble())).toInt()

/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be outside the destination range */
fun Long.convertRange(srcMin: Long, srcMax: Long, dstMin: Long, dstMax: Long): Long = (dstMin + (dstMax - dstMin) * ((this - srcMin).toDouble() / (srcMax - srcMin).toDouble())).toLong()

/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be clamped to the nearest bound */
fun Double.convertRangeClamped(srcMin: Double, srcMax: Double, dstMin: Double, dstMax: Double): Double = convertRange(srcMin, srcMax, dstMin, dstMax).clamp(dstMin, dstMax)

/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be clamped to the nearest bound */
fun Int.convertRangeClamped(srcMin: Int, srcMax: Int, dstMin: Int, dstMax: Int): Int = convertRange(srcMin, srcMax, dstMin, dstMax).clamp(dstMin, dstMax)

/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be clamped to the nearest bound */
fun Long.convertRangeClamped(srcMin: Long, srcMax: Long, dstMin: Long, dstMax: Long): Long = convertRange(srcMin, srcMax, dstMin, dstMax).clamp(dstMin, dstMax)

////////////////////
////////////////////

/** Check if the absolute value of [this] floating point value is small (abs(this) <= 1e-19) */
fun Float.isAlmostZero(): Boolean = abs(this) <= 1e-19

/** Check if the absolute value of [this] floating point value is small (abs(this) <= 1e-19) */
fun Double.isAlmostZero(): Boolean = abs(this) <= 1e-19

/** Check if [this] floating point value is not a number or infinite */
fun Float.isNanOrInfinite() = this.isNaN() || this.isInfinite()

/** Check if [this] floating point value is not a number or infinite */
fun Double.isNanOrInfinite() = this.isNaN() || this.isInfinite()

////////////////////
////////////////////

/** Performs the unsigned modulo between [this] and [other] (negative values would wrap) */
infix fun Int.umod(other: Int): Int {
  val rm = this % other
  val remainder = if (rm == -0) 0 else rm
  return when {
    remainder < 0 -> remainder + other
    else -> remainder
  }
}

/** Performs the unsigned modulo between [this] and [other] (negative values would wrap) */
infix fun Double.umod(other: Double): Double {
  val rm = this % other
  val remainder = if (rm == -0.0) 0.0 else rm
  return when {
    remainder < 0 -> remainder + other
    else -> remainder
  }
}


////////////////////
////////////////////

/** Returns the next value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
fun Int.nextAlignedTo(align: Int) = if (this.isAlignedTo(align)) this else (((this / align) + 1) * align)

/** Returns the next value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
fun Long.nextAlignedTo(align: Long) = if (this.isAlignedTo(align)) this else (((this / align) + 1) * align)

/** Returns the previous value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
fun Int.prevAlignedTo(align: Int) = if (this.isAlignedTo(align)) this else nextAlignedTo(align) - align

/** Returns the previous value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
fun Long.prevAlignedTo(align: Long) = if (this.isAlignedTo(align)) this else nextAlignedTo(align) - align

/** Returns whether [this] is multiple of [alignment] */
fun Int.isAlignedTo(alignment: Int): Boolean = alignment == 0 || (this % alignment) == 0

/** Returns whether [this] is multiple of [alignment] */
fun Long.isAlignedTo(alignment: Long): Boolean = alignment == 0L || (this % alignment) == 0L

////////////////////
////////////////////

/** Clamps [this] value into the range [min] and [max] */
@Deprecated("Use coerceIn in instead", ReplaceWith("this.coerceIn(min, max)"))
fun Int.clamp(min: Int, max: Int): Int = coerceIn(min, max)

/** Clamps [this] value into the range [min] and [max] */
@Deprecated("Use coerceIn in instead", ReplaceWith("this.coerceIn(min, max)"))
fun Long.clamp(min: Long, max: Long): Long = coerceIn(min, max)

/** Clamps [this] value into the range [min] and [max] */
@Deprecated("Use coerceIn in instead", ReplaceWith("this.coerceIn(min, max)"))
fun Double.clamp(min: Double, max: Double): Double = coerceIn(min, max)

/** Clamps [this] value into the range [min] and [max] */
@Deprecated("Use coerceIn in instead", ReplaceWith("this.coerceIn(min, max)"))
fun Float.clamp(min: Float, max: Float): Float = coerceIn(min, max)

/** Clamps [this] value into the range 0 and 1 */
fun Double.clamp01(): Double = clamp(0.0, 1.0)

/** Clamps [this] value into the range 0 and 1 */
fun Float.clamp01(): Float = clamp(0f, 1f)

/** Clamps [this] [Long] value into the range [min] and [max] converting it into [Int]. The default parameters will cover the whole range of values. */
fun Long.toIntClamp(min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Int {
  if (this < min) return min
  if (this > max) return max
  return this.toInt()
}

/** Clamps [this] [Long] value into the range [min] and [max] converting it into [Int] (where [min] must be zero or positive). The default parameters will cover the whole range of positive and zero values. */
fun Long.toUintClamp(min: Int = 0, max: Int = Int.MAX_VALUE) = this.toIntClamp(min, max)

////////////////////
////////////////////

/** Checks if [this] is odd (not multiple of two) */
val Int.isOdd get() = (this % 2) == 1

/** Checks if [this] is even (multiple of two) */
val Int.isEven get() = (this % 2) == 0

////////////////////
////////////////////

/** Returns the next power of two of [this] */
val Int.nextPowerOfTwo: Int
  get() {
    var v = this
    v--
    v = v or (v shr 1)
    v = v or (v shr 2)
    v = v or (v shr 4)
    v = v or (v shr 8)
    v = v or (v shr 16)
    v++
    return v
  }

/** Returns the previous power of two of [this] */
val Int.prevPowerOfTwo: Int get() = if (isPowerOfTwo) this else (nextPowerOfTwo ushr 1)

/** Checks if [this] value is power of two */
val Int.isPowerOfTwo: Boolean get() = this.nextPowerOfTwo == this


/**
 * Returns true if this is between start and end.
 *
 * Attention: start *may* be larger than end
 */
inline fun Double.betweenInclusive(start: Double, end: Double): Boolean {
  return (this >= start) && (this <= end)
    ||
    (this >= end) && (this <= start) //if start < end, this check is relevant
}

/** Clamps the integer value in the 0..255 range */
fun Int.clampUByte(): Int {
  val n = this and -(if (this >= 0) 1 else 0)
  return (n or (255 - n shr 31)) and 0xFF
}

fun almostEquals(a: Float, b: Float) = almostZero(a - b)
fun almostZero(a: Float) = abs(a) <= 0.0000001

fun almostEquals(a: Double, b: Double) = almostZero(a - b)
fun almostZero(a: Double) = abs(a) <= 0.0000001

fun Double.roundDecimalPlaces(places: Int): Double {
  val placesFactor: Double = 10.0.pow(places.toDouble())
  return kotlin.math.round(this * placesFactor) / placesFactor
}

fun Double.ceilDecimalPlaces(places: Int): Double {
  val placesFactor: Double = 10.0.pow(places.toDouble())
  return kotlin.math.ceil(this * placesFactor) / placesFactor
}

fun Double.floorDecimalPlaces(places: Int): Double {
  val placesFactor: Double = 10.0.pow(places.toDouble())
  return kotlin.math.floor(this * placesFactor) / placesFactor
}

fun isEquivalent(a: Double, b: Double, epsilon: Double = 0.0001): Boolean = (a - epsilon < b) && (a + epsilon > b)

fun Double.smoothstep(edge0: Double, edge1: Double): Double {
  val v = (this - edge0) / (edge1 - edge0)
  val step2 = v.clamp(0.0, 1.0)
  return step2 * step2 * (3 - 2 * step2)
}


fun log(v: Int, base: Int): Int = log(v.toDouble(), base.toDouble()).toInt()
fun ln(v: Int): Int = ln(v.toDouble()).toInt()
fun log2(v: Int): Int = log(v.toDouble(), 2.0).toInt()
fun log10(v: Int): Int = log(v.toDouble(), 10.0).toInt()

fun signNonZeroM1(x: Double): Int = if (x <= 0) -1 else +1
fun signNonZeroP1(x: Double): Int = if (x >= 0) +1 else -1

fun Int.nextMultipleOf(multiple: Int) = if (this.isMultipleOf(multiple)) this else (((this / multiple) + 1) * multiple)
fun Long.nextMultipleOf(multiple: Long) = if (this.isMultipleOf(multiple)) this else (((this / multiple) + 1) * multiple)

fun Int.prevMultipleOf(multiple: Int) = if (this.isMultipleOf(multiple)) this else nextMultipleOf(multiple) - multiple
fun Long.prevMultipleOf(multiple: Long) = if (this.isMultipleOf(multiple)) this else nextMultipleOf(multiple) - multiple

fun Int.isMultipleOf(multiple: Int) = multiple == 0 || (this % multiple) == 0
fun Long.isMultipleOf(multiple: Long) = multiple == 0L || (this % multiple) == 0L


val Float.niceStr: String get() = if (almostEquals(this.toLong().toFloat(), this)) "${this.toLong()}" else "$this"
val Double.niceStr: String get() = if (almostEquals(this.toLong().toDouble(), this)) "${this.toLong()}" else "$this"

@PublishedApi
internal fun floorCeil(v: Double): Double = if (v < 0.0) ceil(v) else floor(v)


/**
 * Returns the fallback value if this is NaN
 */
infix fun Double.ifNaN(fallback: Double): Double {
  return if (this.isNaN()) {
    fallback
  } else this
}

/**
 * Executes the action if this is NaN
 */
infix fun Double.ifNaN(action: () -> Double): Double {
  contract {
    callsInPlace(action, InvocationKind.AT_MOST_ONCE)
  }

  return if (this.isNaN()) {
    action()
  } else this
}

/**
 * Executes the action if this is a finite value
 */
inline fun Double.letIfFinite(action: (Double) -> Unit) {
  if (this.isFinite()) {
    action(this)
  }
}

/**
 * Returns the fallback value if this [Double.ifInfinite] returns true
 */
infix fun Double.ifInfinite(fallback: Double): Double {
  return if (this.isInfinite()) {
    fallback
  } else this
}

/**
 * Throws an ISE if this double is not finite
 */
fun Double.requireFinite(): Double {
  if (this.isFinite()) {
    return this
  }

  throw IllegalStateException("Finite value required - but was <$this>")
}

/**
 * Returns 0.0 if this is NaN
 */
inline fun Double.or0ifNaN(): Double {
  return ifNaN(0.0)
}

/**
 * Returns 0.0 if this is Nan or infinite
 */
inline fun Double.or0ifNanOrInfinite(): Double {
  return ifNaN(0.0).ifInfinite(0.0)
}

/**
 * Returns NaN if this is null
 */
inline fun Double?.orNanIfNull(): Double {
  return this ?: Double.NaN
}

/**
 * Returns null if this is 0.0
 */
inline fun Double.orNullif0(): Double? {
  if (this == 0.0) {
    return null
  }

  return this
}

inline fun Double.orNullifNan(): Double? {
  if (this.isNaN()) {
    return null
  }

  return this
}

/**
 * Returns 1.0 if this is NaN
 */
inline fun Double.or1ifNaN(): Double {
  return ifNaN(1.0)
}

/**
 * Returns 1.0 if this is infinite
 */
inline fun Double.or1ifInfinite(): Double {
  return ifInfinite(1.0)
}

/**
 * Returns true if the double is negative
 */
inline fun Double.isNegative(): Boolean {
  return this < 0.0
}

inline fun Double.isPositive(): Boolean {
  return this > 0.0
}

/**
 * Returns true if this is positive or zero
 */
inline fun Double.isPositiveOrZero(): Boolean {
  return this >= 0.0
}

/**
 * Returns the max value or null if all values a null
 */
fun Double?.coerceAtLeastOrNull(other: Double?): Double? {
  if (this == null) {
    return other
  }

  if (other == null) {
    return this
  }

  return this.coerceAtLeast(other)
}

/**
 * Returns the magnitude of this number.
 *
 * * 0.0 -> 0
 * * 1.0 -> 1
 * * 1.0 -> 1
 * * 2.0 -> 1
 * * 10.0 -> 2
 * * 20.0 -> 2
 * * 0.1 -> -1
 * * 0.2 -> -1
 *
 * 10^mag is always >= this
 */
fun Double.findMagnitude(): Int {
  require(this.isPositive()) { "Only supported for positive values but was $this" }

  return log10(this).toIntFloor()
}

fun Double.findMagnitudeValue(): Double {
  return 10.0.pow(this.findMagnitude())
}

/**
 * Returns the magnitude ceil.
 * 10^mag is always >= this
 */
fun Double.findMagnitudeCeil(): Int {
  require(this.isPositive()) { "Only supported for positive values but was $this" }

  return log10(this).toIntCeil()
}

fun Double.findMagnitudeValueCeil(): Double {
  return 10.0.pow(this.findMagnitudeCeil())
}
