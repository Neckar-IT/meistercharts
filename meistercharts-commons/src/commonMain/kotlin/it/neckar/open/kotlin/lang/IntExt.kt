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

import it.neckar.open.unit.other.Exclusive

/**
 * A for loop that starts from 0 until this (exclusive)
 */
inline fun @Exclusive Int.fastFor(callback: (index: Int) -> Unit) {
  for (i in 0 until this) {
    callback(i)
  }
}

/**
 * Supports continuation
 */
inline fun @Exclusive Int.fastForCond(callback: (index: Int) -> Continuation) {
  for (i in 0 until this) {
    val continuation = callback(i)
    if (continuation == Continuation.Break) {
      return
    }
  }
}

enum class Continuation {
  Continue,
  Break
}

/**
 * Calls a callback for each element and one for each space between two numbers.
 * The [separator] is therefore called once less than the [callback].
 */
inline fun Int.join(separator: (indexBefore: Int) -> Unit, callback: (index: Int) -> Unit) {
  if (this == 0) {
    return
  }

  callback(0)
  for (i in 1 until this) {
    separator(i - 1)
    callback(i)
  }
}

/**
 * Maps every integer value from 0 until this (exclusive) and returns a list of the mapped values.
 */
inline fun <V> Int.fastMap(mapper: (value: Int) -> V): List<V> {
  val targetList = mutableListOf<V>()
  this.fastFor {
    targetList.add(mapper(it))
  }
  return targetList
}

/**
 * Checks whether this version is close to the given major version.
 */
fun Int.isCloseTo(expectedValue: Int, maxDelta: Int): Boolean {
  return this >= (expectedValue - maxDelta) && this <= (expectedValue + maxDelta)
}
