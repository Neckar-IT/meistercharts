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
 * This method is especially useful, if it is used on the result of a method call.
 */
fun <T> T.asProvider(): () -> T {
  return { this }
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
