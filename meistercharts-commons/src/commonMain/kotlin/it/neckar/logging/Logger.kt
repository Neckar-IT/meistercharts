package it.neckar.logging

/**
 * Represents a logger.
 *
 * The API is heavily inspired by SLF4j Logger
 */
expect interface Logger {
  /**
   * Returns the name of this logger
   */
  fun getName(): String

  /**
   * Is the logger instance enabled for the DEBUG level?
   *
   * @return True if this Logger is enabled for the DEBUG level, false otherwise.
   */
  fun isDebugEnabled(): Boolean

  /**
   * Log a message at the DEBUG level.
   *
   * @param msg the message string to be logged
   */
  fun debug(msg: String?)

  /**
   * Is the logger instance enabled for the INFO level?
   *
   * @return True if this Logger is enabled for the INFO level, false otherwise.
   */
  fun isInfoEnabled(): Boolean

  /**
   * Log a message at the INFO level.
   *
   * @param msg the message string to be logged
   */
  fun info(msg: String?)

  /**
   * Is the logger instance enabled for the WARN level?
   *
   * @return True if this Logger is enabled for the WARN level, false otherwise.
   */
  fun isWarnEnabled(): Boolean

  /**
   * Log a message at the WARN level.
   *
   * @param msg the message string to be logged
   */
  fun warn(msg: String?)

  /**
   * Is the logger instance enabled for the ERROR level?
   *
   * @return True if this Logger is enabled for the ERROR level,
   * false otherwise.
   */
  fun isErrorEnabled(): Boolean

  /**
   * Log a message at the ERROR level.
   *
   * @param msg the message string to be logged
   */
  fun error(msg: String?)
}

/**
 * Conditional debug action that is only executed if debug is enabled
 */
inline fun Logger.debug(action: () -> String) {
  if (this.isDebugEnabled()) {
    this.debug(action())
  }
}

/**
 * Conditional debug action that is only executed if info is enabled
 */
inline fun Logger.info(action: () -> String) {
  if (this.isInfoEnabled()) {
    this.info(action())
  }
}

/**
 * Conditional debug action that is only executed if warn is enabled
 */
inline fun Logger.warn(action: () -> String) {
  if (this.isWarnEnabled()) {
    this.warn(action())
  }
}

/**
 * Conditional debug action that is only executed if error is enabled
 */
inline fun Logger.error(action: () -> String) {
  if (this.isErrorEnabled()) {
    this.error(action())
  }
}

/**
 * Executes the action if debug is enabled
 */
fun Logger.ifDebug(action: () -> Unit) {
  if (isDebugEnabled()) {
    action()
  }
}
