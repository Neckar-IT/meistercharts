package it.neckar.logging

/**
 * Represents a logger.
 *
 * The API is heavily inspired by SLF4j Logger
 */
actual interface Logger {

  val name: String
    get() {
      return getName()
    }

  /**
   * Returns the name of this logger
   */
  actual fun getName(): String

  /**
   * Is the logger instance enabled for the DEBUG level?
   *
   * @return True if this Logger is enabled for the DEBUG level, false otherwise.
   */
  actual fun isDebugEnabled(): Boolean

  /**
   * Log a message at the DEBUG level.
   *
   * @param msg the message string to be logged
   */
  actual fun debug(msg: String?)

  /**
   * Is the logger instance enabled for the INFO level?
   *
   * @return True if this Logger is enabled for the INFO level, false otherwise.
   */
  actual fun isInfoEnabled(): Boolean

  /**
   * Log a message at the INFO level.
   *
   * @param msg the message string to be logged
   */
  actual fun info(msg: String?)

  /**
   * Is the logger instance enabled for the WARN level?
   *
   * @return True if this Logger is enabled for the WARN level, false otherwise.
   */
  actual fun isWarnEnabled(): Boolean

  /**
   * Log a message at the WARN level.
   *
   * @param msg the message string to be logged
   */
  actual fun warn(msg: String?)

  /**
   * Is the logger instance enabled for the ERROR level?
   *
   * @return True if this Logger is enabled for the ERROR level,
   * false otherwise.
   */
  actual fun isErrorEnabled(): Boolean

  /**
   * Log a message at the ERROR level.
   *
   * @param msg the message string to be logged
   */
  actual fun error(msg: String?)
}
