package it.neckar.open.test.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory

/**
 * Utility class to disable logging for tests.
 */
class LoggingTestSupport {
  companion object {
    /**
     * Disables logging for all loggers.
     */
    fun disableLogging(minLevel: Level = Level.ERROR): OriginalLevels {
      val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

      val originalLevels = loggerContext.getLoggerList().associate { it.name to it.level }
      loggerContext.getLoggerList().forEach { it.level = minLevel }

      return OriginalLevels(originalLevels)
    }

    /**
     * Sets the original levels (again) for all loggers.
     */
    fun enableLogging(originalLevels: OriginalLevels) {
      originalLevels.levels.forEach { (name, originalLevel) ->
        val logger = LoggerFactory.getLogger(name) as ch.qos.logback.classic.Logger
        logger.level = originalLevel
      }
    }
  }

  data class OriginalLevels(val levels: Map<String, Level>)
}
