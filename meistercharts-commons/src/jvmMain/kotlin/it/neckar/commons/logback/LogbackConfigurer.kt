package it.neckar.commons.logback

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.OutputStreamAppender
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.util.FileSize
import ch.qos.logback.core.util.StatusPrinter
import org.slf4j.LoggerFactory
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

/**
 * A configurer for logback logging.<br></br>
 *
 * @see [http://logback.qos.ch/](http://logback.qos.ch/)
 */
object LogbackConfigurer {
  /**
   * Configures the logger for console output only
   */
  fun configureLoggingConsoleOnly(levelForRoot: org.slf4j.event.Level) {
    configureLoggingConsoleOnly(levelForRoot.toLogback())
  }

  /**
   * Configures the logging to use only the provided output stream
   */
  fun configureLoggingToStreamOnly(out: OutputStream, levelForRoot: org.slf4j.event.Level) {
    clearExistingAppenders()
    addStreamAppender(out)
    setRootLoggerLevel(levelForRoot)
  }

  /**
   * Configures the logger for console output only
   */
  fun configureLoggingConsoleOnly(levelForRoot: ch.qos.logback.classic.Level = ch.qos.logback.classic.Level.INFO) {
    clearExistingAppenders()
    addConsoleAppender()

    setRootLoggerLevel(levelForRoot)
  }

  /**
   * Configure logging
   */
  fun configureLoggingConsoleAndFile(logFile: File, levelForRoot: org.slf4j.event.Level) {
    configureLoggingConsoleAndFile(logFile, levelForRoot.toLogback())
  }

  /**
   * Configure (default) logging - for console and file
   */
  fun configureLoggingConsoleAndFile(logFile: File, levelForRoot: ch.qos.logback.classic.Level = ch.qos.logback.classic.Level.INFO) {
    clearExistingAppenders()
    addConsoleAppender()
    addFileAppender(logFile)

    setRootLoggerLevel(levelForRoot)
  }

  /**
   * Removes all existing appenders (e.g. from the BasicConfiguration)
   */
  fun clearExistingAppenders() {
    val rootLogger: Logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
    rootLogger.detachAndStopAllAppenders()
  }

  /**
   * Print the logger status to the given print stream
   */
  fun printStatus(out: PrintStream) {
    val lc = LoggerFactory.getILoggerFactory() as LoggerContext
    StatusPrinter.setPrintStream(out)
    StatusPrinter.print(lc)
  }

  private const val EncoderPattern: String = "[%d{ISO8601}]-[%thread] %-5level %logger - %msg%n"
  const val LogFileName: String = "application.log"

  /**
   * Suffix for logger that are only logged to file
   */
  const val LoggerSuffixFileOnly: String = "_fileOnly"
  const val FileAppenderName: String = "FILE"

  /**
   * Returns the logback classic Logger Context
   */
  val loggerContext: LoggerContext
    get() = LoggerFactory.getILoggerFactory() as LoggerContext

  /**
   * Returns the logback logger for the given name
   */
  private fun getLogbackLogger(loggerName: String): Logger = loggerContext.getLogger(loggerName)

  /**
   * Returns the corresponding logback logger from a slf4j logger
   */
  fun getLogbackLogger(logger: org.slf4j.Logger): Logger {
    return getLogbackLogger(logger.name)
  }

  /**
   * Sets the level for the root logger
   */
  fun setRootLoggerLevel(level: org.slf4j.event.Level) {
    setRootLoggerLevel(level.toLogback())
  }

  fun setRootLoggerLevel(level: ch.qos.logback.classic.Level) {
    val rootLogger = getLogbackLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    rootLogger.level = level
  }

  /**
   * Sets the logger leve for an slf4j logger
   */
  fun setLoggerLevel(logger: org.slf4j.Logger, level: org.slf4j.event.Level) {
    setLoggerLevel(logger.name, level)
  }

  /**
   * Sets the logger level for the logger with the given name
   */
  fun setLoggerLevel(loggerName: String, level: org.slf4j.event.Level) {
    val logger: Logger = getLogbackLogger(loggerName)
    setLoggerLevel(logger, level)
  }

  /**
   * Sets the logger level for a logback logger
   */
  fun setLoggerLevel(logger: Logger, level: org.slf4j.event.Level) {
    logger.level = level.toLogback()
  }

  fun getLoggerLevel(logger: Logger): org.slf4j.event.Level {
    return logger.effectiveLevel.toSlf4j()
  }

  /**
   * Returns the logger level - if there is one
   */
  fun getLoggerLevel(logger: org.slf4j.Logger): org.slf4j.event.Level {
    return logger.toLogback().effectiveLevel.toSlf4j()
  }

  /**
   * Creates and adds a file appender to the root logger.
   *
   * @param logFile the target file where to write the log messages
   */
  fun addFileAppender(logFile: File) {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

    val fileAppender = RollingFileAppender<ILoggingEvent>()
    fileAppender.context = loggerContext
    fileAppender.name = FileAppenderName

    val rollingPolicy = FixedWindowRollingPolicy()
    rollingPolicy.minIndex = 1
    rollingPolicy.maxIndex = 3
    rollingPolicy.fileNamePattern = logFile.absolutePath + ".%i"
    rollingPolicy.context = loggerContext
    rollingPolicy.setParent(fileAppender)

    val triggeringPolicy = SizeBasedTriggeringPolicy<ILoggingEvent>()
    triggeringPolicy.maxFileSize = FileSize.valueOf("5 mb")
    triggeringPolicy.context = loggerContext

    fileAppender.file = logFile.absolutePath
    fileAppender.isAppend = true // mandatory for RollingFileAppender
    fileAppender.rollingPolicy = rollingPolicy
    fileAppender.triggeringPolicy = triggeringPolicy

    rollingPolicy.start()
    triggeringPolicy.start()

    startAppenderAndAddToRoot(loggerContext, fileAppender)
  }

  /**
   * Creates and adds a console appender to the root logger.
   *
   * In most cases [configureLoggingConsoleOnly] should be used instead
   */
  fun addConsoleAppender() {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

    val consoleAppender = ConsoleAppender<ILoggingEvent>()
    consoleAppender.context = loggerContext
    consoleAppender.name = "STDOUT"

    consoleAppender.addFilter(object : Filter<ILoggingEvent>() {
      override fun decide(event: ILoggingEvent): FilterReply {
        return if (event.loggerName.endsWith(LoggerSuffixFileOnly)) {
          FilterReply.DENY
        } else FilterReply.NEUTRAL
      }
    })

    startAppenderAndAddToRoot(loggerContext, consoleAppender)
  }

  /**
   * Configure logging for the given output stream.
   * This method is especially useful for unit tests of logging.
   */
  fun addStreamAppender(sink: OutputStream) {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

    val appender: OutputStreamAppender<ILoggingEvent> = OutputStreamAppender<ILoggingEvent>()
    appender.context = loggerContext //set the context first!
    appender.name = "STREAM_APPENDER"
    appender.outputStream = sink

    appender.addFilter(object : Filter<ILoggingEvent>() {
      override fun decide(event: ILoggingEvent): FilterReply {
        return if (event.loggerName.endsWith(LoggerSuffixFileOnly)) {
          FilterReply.DENY
        } else FilterReply.NEUTRAL
      }
    })

    startAppenderAndAddToRoot(loggerContext, appender)
  }

  private fun startAppenderAndAddToRoot(loggerContext: LoggerContext, appender: OutputStreamAppender<ILoggingEvent>) {
    val encoder = PatternLayoutEncoder()
    encoder.context = loggerContext
    encoder.pattern = EncoderPattern
    encoder.start()

    appender.encoder = encoder
    appender.start()

    val rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    rootLogger.addAppender(appender)
  }
}
