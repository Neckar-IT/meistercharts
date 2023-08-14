package it.neckar.open.http

import kotlin.jvm.JvmInline

/**
 * Represents a URL
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

  //Required for extension methods
  companion object
}
