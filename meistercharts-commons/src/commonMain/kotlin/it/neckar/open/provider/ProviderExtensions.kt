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
package it.neckar.open.provider


inline fun DoublesProvider.fastForEach(callback: (Double) -> Unit) {
  var n = 0
  val currentSize = size()
  while (n < currentSize) {
    callback(this.valueAt(n++))
  }
}

inline fun DoublesProvider.fastForEachIndexed(callback: (index: Int, value: Double) -> Unit) {
  var n = 0
  val currentSize = size()
  while (n < currentSize) {
    callback(n, valueAt(n))
    n++
  }
}

inline fun DoublesProvider.fastForEachIndexed(maxSize: Int, callback: (index: Int, value: Double) -> Unit) {
  var n = 0
  val currentSize = size().coerceAtMost(maxSize)
  while (n < currentSize) {
    callback(n, this.valueAt(n))
    n++
  }
}

inline fun <T> DoublesProvider1<T>.fastForEach(param1: T, callback: (Double) -> Unit) {
  var n = 0
  val currentSize = size(param1)
  while (n < currentSize) {
    callback(this.valueAt(n++, param1))
  }
}

inline fun <T> DoublesProvider1<T>.fastForEachIndexed(param1: T, callback: (index: Int, value: Double) -> Unit) {
  var n = 0
  while (n < size(param1)) {
    callback(n, valueAt(n, param1))
    n++
  }
}

//TODO replace with maxSize(?)
@Deprecated("replace with max size!")
inline fun <T> DoublesProvider1<T>.fastForEachIndexed(actualSize: Int, param1: T, callback: (index: Int, value: Double) -> Unit) {
  var n = 0
  while (n < actualSize) {
    callback(n, this.valueAt(n, param1))
    n++
  }
}

inline fun <T> SizedProvider<T>.fastForEach(callback: (T) -> Unit) {
  var n = 0
  val currentSize = size()
  while (n < currentSize) {
    callback(this.valueAt(n++))
  }
}

/**
 * Returns the max value - but always at least [fallbackValue].
 *
 * If the provider is empty the [fallbackValue] is returned
 */
inline fun <T> SizedProvider<T>.fastMaxBy(fallbackValue: Double = Double.NaN, callback: (value: T) -> Double): Double {
  val currentSize = size()
  if (currentSize == 0) {
    return fallbackValue
  }

  var max = - Double.MAX_VALUE
  var n = 0
  while (n < currentSize) {
    max = callback(this.valueAt(n)).coerceAtLeast(max)
    n++
  }

  return max
}


/**
 * Returns the min value - but always at least [fallbackValue].
 *
 * If the provider is empty the [fallbackValue] is returned
 */
inline fun <T> SizedProvider<T>.fastMinBy(fallbackValue: Double = Double.NaN, callback: (value: T) -> Double): Double {
  val currentSize = size()
  if (currentSize == 0) {
    return fallbackValue
  }

  var min = Double.MAX_VALUE
  var n = 0
  while (n < currentSize) {
    min = callback(this.valueAt(n)).coerceAtMost(min)
    n++
  }

  return min
}


inline fun <T> SizedProvider<T>.fastSumBy(callback: (value: T) -> Double): Double {
  val currentSize = size()
  if (currentSize == 0) {
    return 0.0
  }

  var sum = 0.0
  var n = 0
  while (n < currentSize) {
    sum += callback(this.valueAt(n))
    n++
  }

  return sum
}




inline fun <T> SizedProvider<T>.fastForEachIndexed(callback: (index: Int, value: T) -> Unit) {
  var n = 0
  val currentSize = size()
  while (n < currentSize) {
    callback(n, this.valueAt(n))
    n++
  }
}

inline fun <T> SizedProvider<T>.fastForEachIndexedReversed(callback: (index: Int, value: T) -> Unit) {
  var n = size() - 1
  while (n >= 0) {
    callback(n, this.valueAt(n))
    n--
  }
}

inline fun <T, P1> SizedProvider1<T, P1>.fastForEach(param1: P1, callback: (T) -> Unit) {
  var n = 0
  val currentSize = size(param1)
  while (n < currentSize) {
    callback(this.valueAt(n++, param1))
  }
}

inline fun <T, P1> SizedProvider1<T, P1>.fastForEachIndexed(param1: P1, callback: (index: Int, value: T) -> Unit) {
  var n = 0
  val currentSize = size(param1)
  while (n < currentSize) {
    callback(n, this.valueAt(n, param1))
    n++
  }
}

inline fun <T, P1, P2> SizedProvider2<T, P1, P2>.fastForEach(param1: P1, param2: P2, callback: (T) -> Unit) {
  var n = 0
  val currentSize = size(param1, param2)
  while (n < currentSize) {
    callback(this.valueAt(n++, param1, param2))
  }
}

inline fun <T, P1, P2> SizedProvider2<T, P1, P2>.fastForEachIndexed(param1: P1, param2: P2, callback: (index: Int, value: T) -> Unit) {
  var n = 0
  val currentSize = size(param1, param2)
  while (n < currentSize) {
    callback(n, this.valueAt(n, param1, param2))
    n++
  }
}


inline fun CoordinatesProvider.fastForEach(callback: (x: Double, y: Double) -> Unit) {
  var n = 0
  val currentSize = size()
  while (n < currentSize) {
    callback(this.xAt(n), this.yAt(n))
    n++
  }
}

inline fun CoordinatesProvider.fastForEachIndexed(callback: (index: Int, x: Double, y: Double) -> Unit) {
  var n = 0
  val currentSize = size()
  while (n < currentSize) {
    callback(n, this.xAt(n), this.yAt(n))
    n++
  }
}

inline fun <T> CoordinatesProvider1<T>.fastForEach(param1: T, callback: (x: Double, y: Double) -> Unit) {
  var n = 0
  val currentSize = size(param1)
  while (n < currentSize) {
    callback(this.xAt(n, param1), this.yAt(n, param1))
    n++
  }
}

inline fun <T> CoordinatesProvider1<T>.fastForEachIndexed(param1: T, callback: (index: Int, x: Double, y: Double) -> Unit) {
  var n = 0
  while (n < size(param1)) {
    callback(n, this.xAt(n, param1), this.yAt(n, param1))
    n++
  }
}
