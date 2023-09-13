package it.neckar.open.http

import kotlin.jvm.JvmInline

/**
 * Represents a URL (relative or absolute)
 */
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

  //Required for extension methods
  companion object {
    val root: Url = Url("/")
  }
}
