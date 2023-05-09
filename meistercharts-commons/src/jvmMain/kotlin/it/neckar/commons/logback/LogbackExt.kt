package it.neckar.commons.logback

import ch.qos.logback.classic.Logger


/**
 * Converts a logback level to a slf4j log level
 */
fun ch.qos.logback.classic.Level.toSlf4j(): org.slf4j.event.Level {
  return when (this) {
    ch.qos.logback.classic.Level.ERROR -> org.slf4j.event.Level.ERROR
    ch.qos.logback.classic.Level.WARN -> org.slf4j.event.Level.WARN
    ch.qos.logback.classic.Level.INFO -> org.slf4j.event.Level.INFO
    ch.qos.logback.classic.Level.DEBUG -> org.slf4j.event.Level.DEBUG
    ch.qos.logback.classic.Level.TRACE -> org.slf4j.event.Level.TRACE
    else -> org.slf4j.event.Level.INFO
  }
}

/**
 * Converts a slf4j level to a logback level
 */
fun org.slf4j.event.Level.toLogback(): ch.qos.logback.classic.Level {
  return when (this) {
    org.slf4j.event.Level.ERROR -> ch.qos.logback.classic.Level.ERROR
    org.slf4j.event.Level.WARN -> ch.qos.logback.classic.Level.WARN
    org.slf4j.event.Level.INFO -> ch.qos.logback.classic.Level.INFO
    org.slf4j.event.Level.DEBUG -> ch.qos.logback.classic.Level.DEBUG
    org.slf4j.event.Level.TRACE -> ch.qos.logback.classic.Level.TRACE
  }
}

/**
 * Returns the corresponding logback logger
 */
fun org.slf4j.Logger.toLogback(): Logger {
  return LogbackConfigurer.getLogbackLogger(this)
}

/**
 * Allows setting the level on the logger directly
 */
var org.slf4j.Logger.level: org.slf4j.event.Level
  get() {
    return LogbackConfigurer.getLoggerLevel(this)
  }
  set(level) {
    LogbackConfigurer.setLoggerLevel(this, level)
  }


/**
 * Conditional debug action that is only executed if debug is enabled
 */
inline fun org.slf4j.Logger.debug(action: () -> String) {
  if (this.isDebugEnabled) {
    this.debug(action())
  }
}

/**
 * Conditional debug action that is only executed if info is enabled
 */
inline fun org.slf4j.Logger.info(action: () -> String) {
  if (this.isInfoEnabled) {
    this.info(action())
  }
}

/**
 * Conditional debug action that is only executed if warn is enabled
 */
inline fun org.slf4j.Logger.warn(action: () -> String) {
  if (this.isWarnEnabled) {
    this.warn(action())
  }
}

/**
 * Conditional debug action that is only executed if error is enabled
 */
inline fun org.slf4j.Logger.error(action: () -> String) {
  if (this.isErrorEnabled) {
    this.error(action())
  }
}

