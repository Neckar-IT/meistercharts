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
package it.neckar.logging.impl

import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer
import it.neckar.logging.Logger
import it.neckar.logging.LoggerName
import it.neckar.logging.ShortenedLoggerName
import it.neckar.logging.isEnabled

/**
 * Logger implementation for JS
 */
class LoggerImplJs private constructor(
  override val name: String,
  /**
   * The prefix that is prepended to the log message
   */
  val shortenedLoggerName: ShortenedLoggerName,
) : Logger {
  constructor(name: LoggerName) : this(name.value, name.shortened())

  /**
   * The level for this logger
   */
  var level: Level? = null


  override fun getName(): String {
    return name
  }

  override fun isEnabledForLevel(level: Level): Boolean {
    return level.isEnabled(getEffectiveLogLevel())
  }

  override fun isTraceEnabled(): Boolean {
    return Level.TRACE.isEnabled(getEffectiveLogLevel())
  }

  override fun trace(msg: String?) {
    if (isTraceEnabled()) {
      console.debug("[$shortenedLoggerName] $msg")
    }
  }

  override fun isDebugEnabled(): Boolean {
    return Level.DEBUG.isEnabled(getEffectiveLogLevel())
  }

  override fun debug(msg: String?) {
    if (isDebugEnabled()) {
      console.debug("[$shortenedLoggerName] $msg")
    }
  }

  override fun debug(message: String, objectDebug: Any?) {
    if (isDebugEnabled()) {
      console.debug("[$shortenedLoggerName] $message", objectDebug)
    }
  }

  override fun debug(messageProvider: () -> String, objectDebug: Any?) {
    if (isDebugEnabled()) {
      console.debug("[$shortenedLoggerName] ${messageProvider()}", objectDebug)
    }
  }

  override fun debug(msg: String?, t: Throwable?) {
    if (isDebugEnabled()) {
      console.debug("[$shortenedLoggerName] ${t?.message}", t)
    }
  }

  override fun isInfoEnabled(): Boolean {
    return Level.INFO.isEnabled(getEffectiveLogLevel())
  }

  override fun info(msg: String?) {
    if (isInfoEnabled()) {
      console.info("[$shortenedLoggerName] $msg")
    }
  }

  override fun isWarnEnabled(): Boolean {
    return Level.WARN.isEnabled(getEffectiveLogLevel())
  }

  override fun warn(msg: String?) {
    if (isWarnEnabled()) {
      console.warn("[$shortenedLoggerName] $msg")
    }
  }

  override fun warn(msg: String?, t: Throwable?) {
    if (isWarnEnabled()) {
      console.debug("[$shortenedLoggerName] ${t?.message}", t)
    }
  }

  override fun isErrorEnabled(): Boolean {
    return Level.ERROR.isEnabled(getEffectiveLogLevel())
  }

  override fun error(msg: String?, t: Throwable?) {
    if (isErrorEnabled()) {
      console.debug("[$shortenedLoggerName] ${t?.message}", t)
    }
  }

  /**
   * Returns the effective log level for this logger
   */
  fun getEffectiveLogLevel(): Level {
    return LogConfigurer.getEffectiveLogLevel(this)
  }

  override fun error(msg: String?) {
    if (isErrorEnabled()) {
      console.error("[$shortenedLoggerName] $msg")
    }
  }
}
