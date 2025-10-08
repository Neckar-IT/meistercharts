package it.neckar.runtime.context

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a hostname
 */
@JvmInline
@Serializable
value class Hostname(val value: String) {
  init {
    require(value.isNotBlank()) { "Hostname must not be blank" }
    require(value.contains('@').not()) { "Hostname [$value] must not contain '@'" }
  }

  override fun toString(): String {
    return value
  }

  companion object {
    /**
     * Represents "localhost"
     */
    val localhost: Hostname = Hostname("localhost")

    fun nullable(hostnameAsString: String?): Hostname? {
      if (hostnameAsString == null) {
        return null
      }
      val trimmed = hostnameAsString.trim()
      if (trimmed.isEmpty()) {
        return null
      }
      return Hostname(trimmed)
    }
  }
}
