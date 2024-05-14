package it.neckar.open.http

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents the name of an HTTP parameter
 */
@JvmInline
@Serializable
value class HttpParameterName(val value: String) {
  override fun toString(): String {
    return value
  }

  /**
   * Returns the parameter as a URL pattern parameter which can be used in KTOR routing
   */
  fun asUrlPatternParameter(): String {
    return "{$value}"
  }

  companion object {
    val uuid: HttpParameterName = HttpParameterName("uuid")
    val verbose: HttpParameterName = HttpParameterName("verbose")
  }
}
