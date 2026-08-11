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
 * Values read from an `external interface` are unchecked - Kotlin/JS hands out whatever the JS object holds:
 * an enum property arrives as its plain name string, a property declared `String` may hold a number, and a
 * property the caller left out arrives as `undefined`, which reads as `null` even where the declared type is
 * not nullable. Every value taken from a JS API therefore has to pass a `sanitize` overload before Kotlin
 * code may rely on its declared type.
 *
 * @throws SanitizingFailedException if the value is not one of the entries of [T]
 */
expect inline fun <reified T : Enum<T>> T.sanitize(): T

/**
 * Raises the failure of an enum sanitization.
 *
 * Public because the platform actuals of [sanitize] are `inline` and may only call public API.
 */
fun <E : Enum<E>> throwEnumConversionException(value: Enum<E>, enumEntries: List<E>, e: Throwable?): Nothing {
  throw SanitizingFailedException("Could not sanitize [$value] to Enum.\nPossible values: ${enumEntries.joinToString(", ")}", e)
}

/**
 * Sanitizes a JS boolean.
 *
 * The two comparisons are not redundant: the receiver may hold a number, a string or `undefined` at runtime,
 * and comparing against both boolean values is what rejects those.
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

/**
 * Sanitizes an optional JS boolean.
 *
 * `undefined` cannot be told apart from `null` here and is therefore accepted as the absent value: for a
 * nullable property an omitted value is legal input, not a malformed one.
 */
fun Boolean?.sanitize(): Boolean? {
  if (this == null) {
    return null
  }

  return sanitize()
}

/**
 * Sanitizes a JS double.
 */
fun Double.sanitize(): Double {
  @Suppress("USELESS_IS_CHECK") //Only useless to the compiler: at the JS boundary the runtime type may be anything
  if ((this is Double).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to Double")
  }

  return this
}

/**
 * Sanitizes a JS int.
 */
fun Int.sanitize(): Int {
  @Suppress("USELESS_IS_CHECK") //Only useless to the compiler: at the JS boundary the runtime type may be anything
  if ((this is Int).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to Int")
  }
  return this
}

/**
 * Sanitizes a JS string.
 */
fun String.sanitize(): String {
  @Suppress("USELESS_IS_CHECK") //Only useless to the compiler: at the JS boundary the runtime type may be anything
  if ((this is String).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to String")
  }
  return this
}

/**
 * Sanitizes a JS array.
 *
 * Checks that the receiver is an array at all. The element type is erased and stays unchecked - sanitize the
 * elements individually where they matter.
 */
fun <T> Array<T>.sanitize(): Array<T> {
  @Suppress("USELESS_IS_CHECK") //Only useless to the compiler: at the JS boundary the runtime type may be anything
  if ((this is Array<T>).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to Array<T>")
  }
  return this
}

/**
 * Sanitizes a JS list.
 *
 * A JS caller passes a plain array where the signature asks for a list, so an array is converted rather than
 * rejected. The element type is erased and stays unchecked - sanitize the elements individually where they
 * matter.
 */
fun <T> List<T>.sanitize(): List<T> {
  @Suppress("USELESS_IS_CHECK") //Only useless to the compiler: at the JS boundary the runtime type may be anything
  if (this is List<T>) return this
  //No separate ArrayList check: ArrayList implements List, so the check above already covers it.
  @Suppress("USELESS_IS_CHECK", "UNCHECKED_CAST", "SENSELESS_COMPARISON") //In JS arrays can be passed as lists
  if ((this as Any) is Array<*>) return (this as Array<T>).sanitize().toList()
  throw SanitizingFailedException("Could not sanitize [$this] to List<T>")
}

/**
 * Thrown when a value coming from a JS API does not match its declared Kotlin type.
 */
class SanitizingFailedException(message: String, cause: Throwable? = null) : Exception(message, cause)
