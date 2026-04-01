package it.neckar.projects.common

import kotlin.jvm.JvmInline

/**
 * Represents a port
 */
@JvmInline
value class Port(val value: Int) {
  operator fun plus(value: Int): Port {
    return Port(this.value + value)
  }

  override fun toString(): String {
    return value.toString()
  }

  //Required for extension methods
  companion object {
    val HTTP: Port = Port(80)
    val HTTPS: Port = Port(443)

    fun of(value: Int): Port {
      return Port(value)
    }
  }
}
