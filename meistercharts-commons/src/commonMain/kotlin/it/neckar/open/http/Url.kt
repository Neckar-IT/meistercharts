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
    val endsWithSlash = this.value.endsWith("/")
    val startsWithSlash = relativePath.startsWith("/")

    if (endsWithSlash && startsWithSlash) {
      return Url(value + relativePath.substring(1))
    }

    if (endsWithSlash || startsWithSlash) {
      return Url(value + relativePath)
    }

    return Url("$value/$relativePath")
  }

  operator fun plus(relativeUrl: Url): Url {
    val endsWithSlash = this.value.endsWith("/")
    val startsWithSlash = relativeUrl.value.startsWith("/")

    if (endsWithSlash && startsWithSlash) {
      return Url(value + relativeUrl.value.substring(1))
    }

    if (endsWithSlash || startsWithSlash) {
      return Url(value + relativeUrl.value)
    }

    return Url("$value/${relativeUrl.value}")
  }

  //Required for extension methods
  companion object {
    val root: Url = Url("/")
  }
}
