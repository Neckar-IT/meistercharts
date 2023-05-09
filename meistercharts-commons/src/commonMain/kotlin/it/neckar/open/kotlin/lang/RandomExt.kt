package it.neckar.open.kotlin.lang

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Code adopted from KDS (License: Apache or MIT)
 */

fun Random.ints(): Sequence<Int> = sequence { while (true) yield(nextInt()) }
fun Random.ints(from: Int, until: Int): Sequence<Int> = sequence { while (true) yield(nextInt(from, until)) }
fun Random.ints(range: IntRange): Sequence<Int> = ints(range.start, range.endInclusive + 1)

fun Random.doubles(): Sequence<Double> = sequence { while (true) yield(nextDouble()) }
fun Random.floats(): Sequence<Float> = sequence { while (true) yield(nextFloat()) }

fun <T> List<T>.random(random: Random = Random): T {
  if (this.isEmpty()) throw IllegalArgumentException("Empty list")
  return this[random.nextInt(this.size)]
}

fun <T> List<T>.randomWithWeights(weights: List<Double>, random: Random = Random): T = random.weighted(this.zip(weights).toMap())

operator fun Random.get(min: Double, max: Double): Double = min + nextDouble() * (max - min)
operator fun Random.get(min: Float, max: Float): Float = min + nextFloat() * (max - min)
operator fun Random.get(min: Int, max: Int): Int = min + nextInt(max - min)
operator fun Random.get(range: IntRange): Int = range.start + this.nextInt(range.endInclusive - range.start + 1)
operator fun Random.get(range: LongRange): Long = range.start + this.nextLong() % (range.endInclusive - range.start + 1)
operator fun <T> Random.get(list: List<T>): T = list[this[list.indices]]

fun <T> Random.weighted(weights: Map<T, Double>): T = shuffledWeighted(weights).first()
fun <T> Random.weighted(weights: RandomWeights<T>): T = shuffledWeighted(weights).first()

fun <T> Random.shuffledWeighted(weights: Map<T, Double>): List<T> = shuffledWeighted(RandomWeights(weights))
fun <T> Random.shuffledWeighted(values: List<T>, weights: List<Double>): List<T> = shuffledWeighted(RandomWeights(values, weights))
fun <T> Random.shuffledWeighted(weights: RandomWeights<T>): List<T> {
  val randoms = (weights.items.indices).map { -(nextDouble().pow(1.0 / weights.normalizedWeights[it])) }
  val sortedIndices = (weights.items.indices).sortedWith { a, b -> randoms[a].compareTo(randoms[b]) }
  return sortedIndices.map { weights.items[it] }
}

data class RandomWeights<T>(val weightsMap: Map<T, Double>) {
  constructor(vararg pairs: Pair<T, Double>) : this(mapOf(*pairs))
  constructor(values: List<T>, weights: List<Double>) : this(values.zip(weights).toMap())

  val items: List<T> = weightsMap.keys.toList()
  val weights: List<Double> = weightsMap.values.toList()
  val normalizedWeights: List<Double> = normalizeWeights(weights)

  companion object {
    private fun normalizeWeights(weights: List<Double>): List<Double> {
      val min = weights.minOrNull() ?: 0.0
      return weights.map { (it + min) + 1 }
    }
  }
}

/**
 * Normal distribution.
 * Returns a value around the given center with the given sigma
 *
 * Returns random values around [center]. 95% of these values are within the range of 4 sigma (-2/+2) around the center
 */
fun randomNormal(center: Double, sigma: Double): Double {
  return center + (sigma * sqrt(-2.0 * kotlin.math.log(random.nextDouble(), 10.0)) * cos(2.0 * PI * random.nextDouble()))
}
