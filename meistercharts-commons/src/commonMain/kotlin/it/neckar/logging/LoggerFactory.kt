package it.neckar.logging

/**
 * Creates loggers
 */
expect object LoggerFactory {
  /**
   * Returns the logger for the given logger name
   */
  fun getLogger(loggerName: LoggerName): Logger

  fun getLogger(loggerName: String): Logger
}
