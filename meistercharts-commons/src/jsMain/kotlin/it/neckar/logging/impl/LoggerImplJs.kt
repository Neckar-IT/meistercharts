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

  override fun isErrorEnabled(): Boolean {
    return Level.ERROR.isEnabled(getEffectiveLogLevel())
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
