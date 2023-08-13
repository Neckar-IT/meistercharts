package it.neckar.logging

/**
 * Represents a logger.
 *
 * The API is heavily inspired by SLF4j Logger
 */
actual interface Logger {
  /**
   * The name of this logger
   */
  val name: String
    get() {
      return getName()
    }

  /**
   * Returns the name of this logger
   */
  actual fun getName(): String

  /**
   * Is the logger instance enabled for the TRACE level?
   *
   * @return True if this Logger is enabled for the TRACE level, false otherwise.
   */
  actual fun isTraceEnabled(): Boolean

  /**
   * Log a message at the TRACE level.
   *
   * @param msg the message string to be logged
   */
  actual fun trace(msg: String?)


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

  actual fun debug(msg: String?, t: Throwable?)

  /**
   * Debug the message with the object
   */
  fun debug(message: String, objectDebug: Any?)

  fun debug(messageProvider: () -> String, objectDebug: Any?)

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

  actual fun warn(msg: String?, t: Throwable?)

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

  actual fun error(msg: String?, t: Throwable?)


  fun isEnabledForLevel(level: Level): Boolean
}

actual fun Logger.isEnabledForLevel(level: Level): Boolean {
  return this.isEnabledForLevel(level)
}
