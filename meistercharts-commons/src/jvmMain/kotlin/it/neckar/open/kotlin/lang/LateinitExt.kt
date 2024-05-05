package it.neckar.open.kotlin.lang

import kotlin.reflect.KProperty0

fun <V> KProperty0<V>.ifInitialized(function: (V) -> Unit) {
  require(this.isLateinit) { "Property must be lateinit" }
  val lateinitValue = get()
  if (lateinitValue != null) function(lateinitValue)
}
