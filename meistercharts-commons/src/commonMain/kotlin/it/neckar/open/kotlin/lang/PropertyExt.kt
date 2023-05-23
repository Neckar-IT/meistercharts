package it.neckar.open.kotlin.lang

import kotlin.reflect.KMutableProperty0

/**
 *
 */


/**
 * Gets the values and sets the new value returned by the lambda
 */
inline fun <T> KMutableProperty0<T>.getAndSet(function: (oldValue: T) -> T) {
  set(function(get()))
}

/**
 * Toggles a boolean property
 */
fun KMutableProperty0<Boolean>.toggle() {
  set(!get())
}

