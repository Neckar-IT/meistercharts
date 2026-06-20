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

/**
 * Contains extensions for KDS classes
 *
 */

fun IntArray2.Companion.invokeRows(rows: Array<IntArray>): IntArray2 {
  val width = rows[0].size
  val height = rows.size
  val anyCell = rows[0][0]
  return (IntArray2(width, height) { anyCell }).apply { setRows(rows) }
}

fun IntArray2.setRows(rows: Array<IntArray>) {
  var n = 0
  for (y in rows.indices) {
    val row = rows[y]
    for (x in row.indices) {
      this.data[n++] = row[x]
    }
  }
}

fun IntArray2.Companion.invokeCols(cols: Array<IntArray>): IntArray2 {
  val width = cols.size
  val height = cols[0].size
  val anyCell = cols[0][0]
  return (IntArray2(width, height) { anyCell }).apply { setCols(cols) }
}

fun IntArray2.setCols(cols: Array<IntArray>) {
  for (x in cols.indices) {
    val column = cols[x]
    for (y in column.indices) {
      val value = column[y]
      this[x, y] = value
    }
  }
}

/**
 * Returns a new array with the new width
 */
fun IntArray2.withHeight(newHeight: Int): IntArray2 {
  val newData = IntArray(width * newHeight)
  data.copyInto(newData, 0, 0, data.size)
  return IntArray2(width, newHeight, newData)
}

/**
 * Returns an empty IntArray2
 */
fun IntArray2.Companion.empty(width: Int, height: Int): IntArray2 {
  return IntArray2(width, height, 0)
}

/**
 * Emulates a constructor
 */
@Suppress("FunctionName")
fun IntArrayList(initialSize: Int = 7, fill: (Int) -> Int): IntArrayList {
  return IntArrayList(initialSize).also {
    for (i in 0 until initialSize) {
      it.add(fill(i))
    }
  }
}

/**
 * Emulates a constructor
 */
@Suppress("FunctionName")
fun DoubleArrayList(initialSize: Int = 7, fill: (Int) -> Double): DoubleArrayList {
  return DoubleArrayList(initialSize).also {
    for (i in 0 until initialSize) {
      it.add(fill(i))
    }
  }
}


/**
 * Emulates a constructor
 */
@Suppress("FunctionName")
fun FloatArrayList(initialSize: Int = 7, fill: (Int) -> Float): FloatArrayList {
  return FloatArrayList(initialSize).also {
    for (i in 0 until initialSize) {
      it.add(fill(i))
    }
  }
}

fun DoubleArray2.Companion.invokeCols(cols: Array<DoubleArray>): DoubleArray2 {
  val width = cols.size
  val height = cols[0].size
  val anyCell = cols[0][0]
  return (DoubleArray2(width, height) { anyCell }).apply { setCols(cols) }
}

fun DoubleArray2.setCols(cols: Array<DoubleArray>) {
  for (x in cols.indices) {
    val column = cols[x]
    for (y in column.indices) {
      val value = column[y]
      this[x, y] = value
    }
  }
}

/**
 *
 */
fun IntArrayList.isNotEmpty(): Boolean = !isEmpty()
fun IntArrayList.lastOrNull(): Int? = if (isEmpty()) null else this.getAt(size - 1)
fun IntArrayList.last(): Int = if (isEmpty()) throw NoSuchElementException("Empty array") else this.getAt(size - 1)
fun IntArrayList.first(): Int = if (isEmpty()) throw NoSuchElementException("Empty array") else this.getAt(0)

fun DoubleArrayList.isNotEmpty(): Boolean = !isEmpty()
fun DoubleArrayList.lastOrNull(): Double? = if (isEmpty()) null else this.getAt(size - 1)
fun DoubleArrayList.last(): Double = if (isEmpty()) throw NoSuchElementException("Empty array") else this.getAt(size - 1)
fun DoubleArrayList.first(): Double = if (isEmpty()) throw NoSuchElementException("Empty array") else this.getAt(0)

fun FloatArrayList.isNotEmpty(): Boolean = !isEmpty()
fun FloatArrayList.lastOrNull(): Float? = if (isEmpty()) null else this.getAt(size - 1)
fun FloatArrayList.last(): Float = if (isEmpty()) throw NoSuchElementException("Empty array") else this.getAt(size - 1)
fun FloatArrayList.first(): Float = if (isEmpty()) throw NoSuchElementException("Empty array") else this.getAt(0)

fun DoubleArray.minOrElse(nil: Double): Double {
  if (isEmpty()) return nil
  var out = Double.POSITIVE_INFINITY
  for (i in 0..lastIndex) out = kotlin.math.min(out, this[i])
  return out
}

fun DoubleArray.maxOrElse(nil: Double): Double {
  if (isEmpty()) return nil
  var out = Double.NEGATIVE_INFINITY
  for (i in 0..lastIndex) out = kotlin.math.max(out, this[i])
  return out
}


/**
 * Removes the elements if the given predicate returns true
 */
fun DoubleArrayList.removeAll(predicate: (Double) -> Boolean) {
  var n = 0
  while (n < size) {
    val currentValue = this.getAt(n)

    if (predicate(currentValue)) {
      removeAt(n).also { removed ->
        check(removed == currentValue) { "Expected to remove $currentValue at index $n but removed $removed" }
      }
    } else {
      //Only iterate if no element has been removed
      n++
    }
  }
}

fun DoubleArrayList.fastContains(value: Double): Boolean {
  this.fastForEach {
    if (it == value) {
      return true
    }
  }

  return false
}
