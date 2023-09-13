package it.neckar.open.http

import kotlin.jvm.JvmInline

/**
 * Represents a port
 */
@JvmInline
value class Port(val value: Int) {
  override fun toString(): String {
    return value.toString()
  }

  //Required for extension methods
  companion object

}
