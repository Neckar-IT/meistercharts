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

  /**
   * Appends something to the URL
   */
  operator fun plus(toAppend: String): Url {
    if (toAppend.startsWith("?")) {
      return Url(value + toAppend)
    }

    val endsWithSlash = this.value.endsWith("/")
    val startsWithSlash = toAppend.startsWith("/")

    if (endsWithSlash && startsWithSlash) {
      return Url(value + toAppend.substring(1))
    }

    if (endsWithSlash || startsWithSlash) {
      return Url(value + toAppend)
    }

    return Url("$value/$toAppend")
  }

  operator fun plus(toAppend: Url): Url {
    val endsWithSlash = this.value.endsWith("/")
    val startsWithSlash = toAppend.value.startsWith("/")

    if (endsWithSlash && startsWithSlash) {
      return Url(value + toAppend.value.substring(1))
    }

    if (endsWithSlash || startsWithSlash) {
      return Url(value + toAppend.value)
    }

    return Url("$value/${toAppend.value}")
  }

  //Required for extension methods
  companion object {
    val root: Url = Url("/")
  }
}
