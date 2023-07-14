package it.neckar.open.kotlin.lang

import kotlin.reflect.KMutableProperty0

/**
 * Sets the boolean property to true (e.g. for a loading state), executes the block and sets the boolean property to false again.
 */
fun KMutableProperty0<Boolean>.use(block: () -> Unit) {
  if (get()) {
    throw IllegalStateException("Must only be called with a value of false for ${this.name}")
  }

  set(true)
  try {
    block()
  } finally {
    set(false)
  }
}

fun <T> KMutableProperty0<T>.use(busyValue: T, block: () -> Unit) {
  val originalValue = get()
  if (originalValue == busyValue) {
    throw IllegalStateException("Already set to busy value $busyValue for ${this.name}")
  }

  this.set(busyValue)
  try {
    block()
  } finally {
    set(originalValue)
  }
}
