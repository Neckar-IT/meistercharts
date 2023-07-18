package it.neckar.logging

import kotlin.jvm.JvmInline

/**
 * Represents a shortened logger name.
 */
@JvmInline
value class ShortenedLoggerName(val value: String) {
  /**
   * Returns true if the given logger name matches this shortened logger name.
   */
  fun matches(loggerName: LoggerName): Boolean {
    return loggerName.shortened().value == value
  }

  override fun toString(): String {
    return value
  }
}
