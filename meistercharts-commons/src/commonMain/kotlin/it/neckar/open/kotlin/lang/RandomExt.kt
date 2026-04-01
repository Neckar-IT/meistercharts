/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
operator fun Random.get(range: LongRange): Long = nextLong(range.start, range.endInclusive + 1)
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


/**
 * Creates a random string of the given length using the provided dictionary
 */
fun Random.nextString(length: Int, dictionary: String): String {
  return nextString(length, dictionary.toCharArray())
}

fun Random.nextString(length: Int, dictionary: CharArray): String {
  val chars = CharArray(length)
  val dictionarySize = dictionary.size

  for (index in 0 until length) {
    chars[index] = dictionary[nextInt(dictionarySize)]
  }

  return chars.concatToString()
}
