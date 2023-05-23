package it.neckar.logging.impl

import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer
import it.neckar.logging.Logger
import it.neckar.logging.isEnabled

class LoggerImplJs(override val name: String) : Logger {
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
    console.debug(msg)
  }

  override fun isInfoEnabled(): Boolean {
    return Level.INFO.isEnabled(getEffectiveLogLevel())
  }

  override fun info(msg: String?) {
    console.info(msg)
  }

  override fun isWarnEnabled(): Boolean {
    return Level.WARN.isEnabled(getEffectiveLogLevel())
  }

  override fun warn(msg: String?) {
    console.warn(msg)
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
    console.error(msg)
  }
}
