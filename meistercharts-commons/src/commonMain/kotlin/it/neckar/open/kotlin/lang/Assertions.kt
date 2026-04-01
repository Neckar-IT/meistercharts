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

import kotlin.contracts.contract

/**
 * Requires that both parameters are equal.
 * Uses the provided lazy message appended with first/second
 * @throws IllegalArgumentException if first and second are not equal
 */
fun <T> requireEquals(first: T, second: T, lazyMessage: () -> Any) {
  require(first == second) {
    "${lazyMessage()}: <$first> != <$second>"
  }
}

/**
 * Checks that both parameters are equal.
 * Uses the provided lazy message appended with first/second
 * @throws IllegalStateException if first and second are not equal
 */
fun <T> checkEquals(first: T, second: T, lazyMessage: () -> Any) {
  check(first == second) {
    "${lazyMessage()}: <$first> != <$second>"
  }
}

/**
 * Suffix notation for checkNotNull
 */
@IgnorableReturnValue
inline fun <T> T?.checkNotNull(lazyMessage: () -> Any = { "Must not be null" }): T {
  contract {
    returns() implies (this@checkNotNull != null)
  }
  return checkNotNull(this, lazyMessage)
}

@IgnorableReturnValue
inline fun <T> T?.requireNotNull(message: String): T {
  contract {
    returns() implies (this@requireNotNull != null)
  }
  return requireNotNull(this) {
    message
  }
}

@IgnorableReturnValue
inline fun <T> T?.requireNotNull(lazyMessage: () -> Any = { "Must not be null" }): T {
  contract {
    returns() implies (this@requireNotNull != null)
  }
  return requireNotNull(this, lazyMessage)
}

/**
 * Throws an exception if this list is empty
 */
inline fun <T> List<T>.checkNotEmpty(lazyMessage: () -> Any = { "Must not be empty" }): List<T> {
  check(isNotEmpty(), lazyMessage)
  return this
}
