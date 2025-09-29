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

    /**
     * Helper method for refactorings
     */
    @Deprecated("Use the constructor instead", ReplaceWith("port"))
    operator fun invoke(port: Port): Port {
      return port
    }

    fun of(value: Int): Port {
      return Port(value)
    }
  }
}
