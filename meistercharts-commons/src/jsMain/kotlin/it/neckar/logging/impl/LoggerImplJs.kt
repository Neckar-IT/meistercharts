package it.neckar.logging.impl

import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer
import it.neckar.logging.Logger
import it.neckar.logging.isEnabled

class LoggerImplJs(
  override val name: String,
  /**
   * The prefix that is prepended to the log message
   */
  val prefix: String,

  ) : Logger {
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
      console.debug("[$prefix] $msg")
    }
  }

  override fun isInfoEnabled(): Boolean {
    return Level.INFO.isEnabled(getEffectiveLogLevel())
  }

  override fun info(msg: String?) {
    if (isInfoEnabled()) {
      console.info("[$prefix] $msg")
    }
  }

  override fun isWarnEnabled(): Boolean {
    return Level.WARN.isEnabled(getEffectiveLogLevel())
  }

  override fun warn(msg: String?) {
    if (isWarnEnabled()) {
      console.warn("[$prefix] $msg")
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
      console.error("[$prefix] $msg")
    }
  }
}
