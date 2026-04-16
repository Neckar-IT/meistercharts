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
package it.neckar.open.collections

inline fun IntRange.toIntList(): IntArrayList = IntArrayList(this.endInclusive - this.start).also { for (v in this.start..this.endInclusive) it.add(v) }

inline fun Iterable<Int>.toIntList(): IntArrayList = IntArrayList().also { for (v in this) it.add(v) }
inline fun Iterable<Float>.toFloatList(): FloatArrayList = FloatArrayList().also { for (v in this) it.add(v) }
inline fun Iterable<Double>.toDoubleList(): DoubleArrayList = DoubleArrayList().also { for (v in this) it.add(v) }

//  MAP
inline fun <T> Iterable<T>.mapInt(callback: (T) -> Int): IntArrayList = IntArrayList().also { for (v in this) it.add(callback(v)) }
inline fun <T> Iterable<T>.mapFloat(callback: (T) -> Float): FloatArrayList = FloatArrayList().also { for (v in this) it.add(callback(v)) }
inline fun <T> Iterable<T>.mapDouble(callback: (T) -> Double): DoubleArrayList = DoubleArrayList().also { for (v in this) it.add(callback(v)) }

// FILTER
inline fun IntArrayList.filter(callback: (Int) -> Boolean): IntArrayList = IntArrayList().also { for (v in this) if (callback(v)) it.add(v) }
inline fun FloatArrayList.filter(callback: (Float) -> Boolean): FloatArrayList = FloatArrayList().also { for (v in this) if (callback(v)) it.add(v) }
inline fun DoubleArrayList.filter(callback: (Double) -> Boolean): DoubleArrayList = DoubleArrayList().also { for (v in this) if (callback(v)) it.add(v) }

private object IntArrayListSortOps : SortOps<IntArrayList>() {
  override fun compare(subject: IntArrayList, l: Int, r: Int): Int = subject.getAt(l).compareTo(subject.getAt(r))
  override fun swap(subject: IntArrayList, indexL: Int, indexR: Int) {
    val l = subject.getAt(indexL)
    val r = subject.getAt(indexR)
    subject[indexR] = l
    subject[indexL] = r
  }
}

private object DoubleArrayListSortOps : SortOps<DoubleArrayList>() {
  override fun compare(subject: DoubleArrayList, l: Int, r: Int): Int = subject.getAt(l).compareTo(subject.getAt(r))
  override fun swap(subject: DoubleArrayList, indexL: Int, indexR: Int) {
    val l = subject.getAt(indexL)
    val r = subject.getAt(indexR)
    subject[indexR] = l
    subject[indexL] = r
  }
}

private object FloatArrayListSortOps : SortOps<FloatArrayList>() {
  override fun compare(subject: FloatArrayList, l: Int, r: Int): Int = subject.getAt(l).compareTo(subject.getAt(r))
  override fun swap(subject: FloatArrayList, indexL: Int, indexR: Int) {
    val l = subject.getAt(indexL)
    val r = subject.getAt(indexR)
    subject[indexR] = l
    subject[indexL] = r
  }
}

@IgnorableReturnValue
fun IntArrayList.sort(start: Int = 0, end: Int = size, reversed: Boolean = false) = genericSort(this, start, end - 1, IntArrayListSortOps, reversed)

@IgnorableReturnValue
fun DoubleArrayList.sort(start: Int = 0, end: Int = size, reversed: Boolean = false) = genericSort(this, start, end - 1, DoubleArrayListSortOps, reversed)

@IgnorableReturnValue
fun FloatArrayList.sort(start: Int = 0, end: Int = size, reversed: Boolean = false) = genericSort(this, start, end - 1, FloatArrayListSortOps, reversed)

fun IntArrayList.reverse(start: Int = 0, end: Int = size) = IntArrayListSortOps.reverse(this, start, end - 1)

fun DoubleArrayList.reverse(start: Int = 0, end: Int = size) = DoubleArrayListSortOps.reverse(this, start, end - 1)

fun FloatArrayList.reverse(start: Int = 0, end: Int = size) = FloatArrayListSortOps.reverse(this, start, end - 1)

fun DoubleArrayList.toIntArrayList(): IntArrayList {
  val out = IntArrayList(this.size)
  this.fastForEach { out.add(it.toInt()) }
  return out
}

fun IntArrayList.toIntArrayList(): DoubleArrayList {
  val out = DoubleArrayList(this.size)
  this.fastForEach { out.add(it.toDouble()) }
  return out
}

fun <T> MutableList<T>.swap(lIndex: Int, rIndex: Int) {
  val temp = this[lIndex]
  this[lIndex] = this[rIndex]
  this[rIndex] = temp
}

fun <T> List<T>.rotated(offset: Int): List<T> = ArrayList<T>(this.size).also {
  for (n in 0 until this.size) it.add(this.getCyclic(n - offset))
}

fun IntArrayList.rotated(offset: Int): IntArrayList = IntArrayList(this.size).also {
  for (n in 0 until this.size) it.add(this.getCyclic(n - offset))
}

fun FloatArrayList.rotated(offset: Int): FloatArrayList = FloatArrayList(this.size).also {
  for (n in 0 until this.size) it.add(this.getCyclic(n - offset))
}

fun DoubleArrayList.rotated(offset: Int): DoubleArrayList = DoubleArrayList(this.size).also {
  for (n in 0 until this.size) it.add(this.getCyclic(n - offset))
}

/**
 * Removes elements from the start of the list until the given [maxSize] has been reached.
 *
 * Uses a single bulk [DoubleArrayList.removeAt] call instead of a shrinking loop —
 * O(n) instead of O(n²).
 */
fun DoubleArrayList.deleteFromStartUntilMaxSize(maxSize: Int) {
  require(maxSize >= 0) { "Invalid max size: $maxSize" }

  val removeCount = this.size - maxSize
  if (removeCount > 0) {
    val firstExpected = this.getAt(0)
    removeAt(0, removeCount).also { firstRemoved ->
      check(firstRemoved == firstExpected) { "Expected first removed element to be $firstExpected but was $firstRemoved" }
    }
  }
}
