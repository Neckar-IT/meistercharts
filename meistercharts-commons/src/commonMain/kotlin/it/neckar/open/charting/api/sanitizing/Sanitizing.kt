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
package it.neckar.open.charting.api.sanitizing

/**
 * Ensures that this enum value is in fact an enum value.
 *
 * This is a workaround because the enum values we receive are actually of type string.
 */
expect inline fun <reified T : Enum<T>> T.sanitize(): T

/**
 * Helper method that throws an exception
 */
fun <E : Enum<E>> throwEnumConversionException(value: Enum<E>, enumEntries: List<E>, e: Throwable?): Nothing {
  throw SanitizingFailedException("Could not sanitize [$value] to Enum.\nPossible values: ${enumEntries.joinToString(", ")}", e)
}

/**
 * Sanitizes a JS boolean
 */
@Suppress("SimplifyBooleanWithConstants")
fun Boolean.sanitize(): Boolean {
  return when {
    this == true -> {
      true
    }

    this == false -> {
      false
    }

    else -> throw SanitizingFailedException("Could not sanitize [$this] to Boolean")
  }
}

fun Boolean?.sanitize(): Boolean? {
  if (this == null) {
    return null
  }

  return sanitize()
}

/**
 * Sanitizes a JS double
 */
fun Double.sanitize(): Double {
  @Suppress("USELESS_IS_CHECK") //undefined could be null
  if ((this is Double).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to Double")
  }

  return this
}

fun Int.sanitize(): Int {
  @Suppress("USELESS_IS_CHECK") //undefined could be null
  if ((this is Int).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to Int")
  }
  return this
}

fun String.sanitize(): String {
  @Suppress("USELESS_IS_CHECK") //undefined could be null
  if ((this is String).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to String")
  }
  return this
}

fun <T> Array<T>.sanitize(): Array<T> {
  @Suppress("USELESS_IS_CHECK") //undefined could be null
  if ((this is Array<T>).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to Array<T>")
  }
  return this
}

fun <T> List<T>.sanitize(): List<T> {
  @Suppress("USELESS_IS_CHECK") //undefined could be null
  if (this is List<T>) return this
  if (this is ArrayList<T>) return this
  @Suppress("USELESS_IS_CHECK", "UNCHECKED_CAST", "SENSELESS_COMPARISON") //In JS arrays can be passed as lists
  if ((this as Any) is Array<*>) return (this as Array<T>).sanitize().toList()
  throw SanitizingFailedException("Could not sanitize [$this] to List<T>")
}

class SanitizingFailedException(message: String, cause: Throwable? = null) : Exception(message, cause)
