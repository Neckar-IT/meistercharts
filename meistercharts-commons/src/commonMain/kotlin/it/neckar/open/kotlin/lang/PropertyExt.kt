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
 * Sets the new value and calls the onChange lambda if the value has changed.
 * [onChange] is called after the value has been set.
 */
inline fun <T> KMutableProperty0<T>.setIfDifferent(newValue: T, onChange: () -> Unit) {
  if (get() != newValue) {
    set(newValue)
    onChange()
  }
}

/**
 * Toggles a boolean property
 */
fun KMutableProperty0<Boolean>.toggle() {
  set(get().not())
}

