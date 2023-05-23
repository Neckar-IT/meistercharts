package it.neckar.logging

/**
 * Creates loggers
 */
expect object LoggerFactory {
  /**
   * Returns the logger for the given name
   */
  fun getLogger(loggerName: String): Logger
}
