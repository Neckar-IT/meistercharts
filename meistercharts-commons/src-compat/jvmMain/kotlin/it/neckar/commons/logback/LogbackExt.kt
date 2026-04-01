package it.neckar.commons.logback

import ch.qos.logback.classic.Logger

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

fun org.slf4j.event.Level.toLogback(): ch.qos.logback.classic.Level {
  return when (this) {
    org.slf4j.event.Level.ERROR -> ch.qos.logback.classic.Level.ERROR
    org.slf4j.event.Level.WARN -> ch.qos.logback.classic.Level.WARN
    org.slf4j.event.Level.INFO -> ch.qos.logback.classic.Level.INFO
    org.slf4j.event.Level.DEBUG -> ch.qos.logback.classic.Level.DEBUG
    org.slf4j.event.Level.TRACE -> ch.qos.logback.classic.Level.TRACE
  }
}

fun org.slf4j.Logger.toLogback(): Logger {
  return this as Logger
}

var org.slf4j.Logger.level: org.slf4j.event.Level
  get() {
    val logbackLogger = this.toLogback()
    return (logbackLogger.level ?: logbackLogger.effectiveLevel).toSlf4j()
  }
  set(level) {
    this.toLogback().level = level.toLogback()
  }
