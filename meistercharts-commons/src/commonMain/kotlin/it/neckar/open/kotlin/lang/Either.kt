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

/**
 * Realization of this interface will contain at least one not null - [value1] or [value2]
 */
data class Either<T1, T2>(
  val value1: T1?,
  val value2: T2?,
) {
  init {
    require(value1 != null || value2 != null) { "Either must contain one value, but contains two" }
    require(value1 == null || value2 == null) { "Either must contain one value, but does not contain any" }
  }

  /**
   * Will call [block] in case when [first] is not null
   */
  inline fun onFirst(block: (T1) -> Unit): Either<T1, T2> {
    value1?.let {
      block(it)
    }

    return this
  }

  /**
   * Will call [block] in case when [second] is not null
   */
  inline fun onSecond(block: (T2) -> Unit): Either<T1, T2> {
    value2?.let {
      block(it)
    }

    return this
  }

  /**
   * @return Result of [block] if this contains a first value
   */
  inline fun <R> mapOnFirst(block: (T1) -> R): R? {
    return value1?.let {
      block(it)
    }
  }

  /**
   * @return Result of [block] if this contains a second value
   */
  inline fun <R> mapOnSecond(block: (T2) -> R): R? {
    return value2?.let {
      block(it)
    }
  }
}


