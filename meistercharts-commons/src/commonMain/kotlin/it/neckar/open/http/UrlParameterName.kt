package it.neckar.open.http

import it.neckar.open.annotations.TsExport
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents the name of an HTTP parameter
 */
@JvmInline
@Serializable
@TsExport
value class UrlParameterName(val value: String) : UrlPathSegment {
  override fun toString(): String {
    return asUrlPatternParameter()
  }

  /**
   * Returns the parameter as a URL pattern parameter which can be used in KTOR routing
   */
  fun asUrlPatternParameter(): String {
    return "{$value}"
  }

  companion object {
    val uuid: UrlParameterName = UrlParameterName("uuid")
    val verbose: UrlParameterName = UrlParameterName("verbose")
  }
}
