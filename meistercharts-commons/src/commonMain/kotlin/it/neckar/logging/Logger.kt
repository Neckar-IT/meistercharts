/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

  //Currently not working -> https://youtrack.jetbrains.com/issue/KT-59785/
  ///**
  // * Returns true if the logger is enabled for the provided level
  // */
  //fun isEnabledForLevel(level: Level): Boolean

  /**
   * Is the logger instance enabled for the TRACE level?
   *
   * @return True if this Logger is enabled for the TRACE level, false otherwise.
   */
  fun isTraceEnabled(): Boolean

  /**
   * Log a message at the TRACE level.
   *
   * @param msg the message string to be logged
   */
  fun trace(msg: String?)

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

  fun debug(msg: String?, t: Throwable?)

  /**
   * Debugs the provided object.
   *
   * ATTENTION: This is a workaround to be able to serialize objects in JS.
   * It behaves differently on the JVM where the [message] is interpreted as format.
   */
  @Deprecated("Only use in special cases!!!!")
  fun debug(message: String, objectDebug: Any?);

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

  fun warn(msg: String?, t: Throwable?)

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

  fun error(msg: String?, t: Throwable?)
}

/**
 * Returns the logger name of this logger.
 */
inline val Logger.loggerName: LoggerName
  //Must be an extension val, because we use a typealias on JVM side
  get() = LoggerName(getName())


expect fun Logger.isEnabledForLevel(level: Level): Boolean

/**
 * Conditional log statement that is only executed if the given level is enabled
 */
inline fun Logger.log(level: Level, messageProvider: () -> String) {
  if (isEnabledForLevel(level)) {
    val message = messageProvider()

    when (level) {
      Level.TRACE -> trace(message)
      Level.DEBUG -> debug(message)
      Level.INFO -> info(message)
      Level.WARN -> warn(message)
      Level.ERROR -> error(message)
      else -> throw UnsupportedOperationException("Unsupported log level $level")
    }
  }
}

/**
 * Conditional trace statement that is only executed if trace is enabled
 */
inline fun Logger.trace(messageProvider: () -> String) {
  if (isTraceEnabled()) {
    trace(messageProvider())
  }
}

/**
 * Conditional debug statement that is only executed if debug is enabled
 */
inline fun Logger.debug(messageProvider: () -> String) {
  if (isDebugEnabled()) {
    debug(messageProvider())
  }
}

/**
 * Conditional info statement that is only executed if info is enabled
 */
inline fun Logger.info(messageProvider: () -> String) {
  if (isInfoEnabled()) {
    info(messageProvider())
  }
}

/**
 * Conditional warn statement that is only executed if warn is enabled
 */
inline fun Logger.warn(messageProvider: () -> String) {
  if (isWarnEnabled()) {
    warn(messageProvider())
  }
}

/**
 * Conditional error statement that is only executed if error is enabled
 */
inline fun Logger.error(messageProvider: () -> String) {
  if (isErrorEnabled()) {
    error(messageProvider())
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
