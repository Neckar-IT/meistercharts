package it.neckar.commons.js

import kotlin.jvm.JvmInline

/**
 * Represents a cookie name
 */
@JvmInline
value class CookieName(val value: String) {
  override fun toString(): String {
    return value
  }
}
