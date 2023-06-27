package it.neckar.logging

import kotlin.jvm.JvmInline

/**
 * Represents a logger name.
 */
@JvmInline
value class LoggerName(val value: String) {
  /**
   * Returns a shortened version of the logger name.
   */
  fun shortened(): ShortenedLoggerName {
    val parts = value.split('.')
    if (parts.isEmpty()) return ShortenedLoggerName(value)

    return ShortenedLoggerName(buildString {
      for (i in 0 until parts.size - 1) {
        append(parts[i][0]) //add the first char of the part
        append('.')
      }

      append(parts.last())
    })
  }

  override fun toString(): String {
    return value
  }
}
