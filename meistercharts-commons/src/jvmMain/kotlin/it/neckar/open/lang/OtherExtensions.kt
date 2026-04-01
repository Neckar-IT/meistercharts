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
package it.neckar.open.lang

import java.net.URL
import java.util.Optional
import java.util.UUID


/**
 * Returns value or null from Optional. Useful when using kotlin-like T? and Optional<T>.
 * */
fun <T> Optional<T>.orNull(): T? = this.orElse(null)

/**
 * Creates a string like "className(description)", for example "Double(42.0)"
 * Useful e.g. for implementing .toString() override.
 *
 * @param description the content to be displayed inside the brackets.
 * @param brackets a two-character string like "{}", default = "()".
 * @param className the string to be displayed before the brackets; default = the class name of [this].
 */
fun Any.toLongString(description: String, brackets: String = "()", className: String? = null): String {
  val actualClassName = className ?: this.javaClass.simpleName
  val actualBrackets = if (brackets.length == 2) brackets else "<>"
  return "$actualClassName${actualBrackets[0]}$description${actualBrackets[1]}"
}

/**
 * Check whether a given string is a valid UUID.
 *
 * @param candidateUUID A candidate UUID to be checked.
 * @return true iff [candidateUUID] is a valid UUID.
 */
fun isUUID(candidateUUID: String): Boolean = runCatching { UUID.fromString(candidateUUID) }.isSuccess

/**
 * Check whether a given string is a valid URL.
 *
 * Please note that this function is not all mighty and it only tries to convert the given string to URL and then to URI.
 * This means that it fails to recognize invalid urls such as  `https://sm.ai,` or `https://`.
 * For more complex validation, one should probably use Apache URL Validator.
 *
 * For the sample cases when this simple method fails please see `isUrlFalsePositives` test in the file OtherExtensionsTest.kt.
 *
 * @param candidateUrl A candidate URL to be checked.
 * @return true iff [candidateUrl] is a valid URL.
 */
fun isURL(candidateUrl: String): Boolean = runCatching { URL(candidateUrl).toURI() }.isSuccess

/**
 * Retrieves environment variable from the system.
 */
fun getEnv(variableName: String): String? = System.getenv(variableName)

/**
 * Shortcut for [System.lineSeparator].
 */
val newLine: String get() = System.lineSeparator()
