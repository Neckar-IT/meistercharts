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

import kotlin.reflect.KMutableProperty0

/**
 * The file contains extension methods related to lambda methods
 */

/**
 * Connects two lambdas. Returns a lambda that combines both this and the given function
 */
fun <T> (T.() -> Unit).and(function: T.() -> Unit): T.() -> Unit {
  return wrapped(this, function)
}

/**
 * Wraps the current lambda from the given property and stores the wrapped lambda
 */
fun <T> KMutableProperty0<T.() -> Unit>.wrapped(function: T.() -> Unit) {
  val originalLambda = get()
  set(wrapped(originalLambda, function))
}

/**
 * Connects two lambdas. Returns a lambda that combines both the delegate and the given function
 */
fun <T> wrapped(delegate: T.() -> Unit, function: T.() -> Unit): T.() -> Unit {
  return {
    delegate()
    function()
  }
}

/**
 * Converts a constant value to a provider.
 *
 * This method is especially useful if it is used on the result of a method call.
 */
fun <T> T.asProvider(): () -> T {
  return { this }
}

fun <T> T?.asProvider(fallback: () -> T): () -> T {
  return { this ?: fallback() }
}

/**
 * Converts a constant value to a provider with 1 parameter
 */
fun <T> T.asProvider1(): (Any) -> T {
  return { this }
}

/**
 * Converts a constant value to a provider with 2 parameters
 */
fun <T> T.asProvider2(): (Any, Any) -> T {
  return { _, _ -> this }
}

/**
 * Converts a constant value to a provider with 3 parameters
 */
fun <T> T.asProvider3(): (Any, Any, Any) -> T {
  return { _, _, _ -> this }
}
