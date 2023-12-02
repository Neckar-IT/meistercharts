package it.neckar.open.http

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a URL (relative or absolute)
 */
@Serializable
@JvmInline
value class Url(val value: String) {
  fun isAbsoluteUrl(): Boolean {
    return value.startsWith("/")
  }

  fun isExternalUrl(): Boolean {
    return value.startsWith("http")
  }

  override fun toString(): String {
    return value
  }

  operator fun plus(relativePath: String): Url {
    return Url(value + relativePath)
  }
  operator fun plus(relativeUrl: Url): Url {
    require(relativeUrl.isAbsoluteUrl().not()){
      "relativeUrl ${relativeUrl} must be relative"
    }
    return Url(value + relativeUrl.value)
  }

  //Required for extension methods
  companion object {
    val root: Url = Url("/")
  }
}
