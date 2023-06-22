package it.neckar.logging

import it.neckar.logging.impl.LoggerImplJs


/**
 * Configures the log levels for JS
 */
object LogConfigurer {
  /**
   * The root logger level
   */
  var rootLevel: Level = Level.DEBUG
    private set

  /**
   * Sets the root logger level
   */
  fun setRootLoggerLevel(logLevel: Level) {
    rootLevel = logLevel
  }

  /**
   * Sets the log level for a given logger
   */
  fun setLogLevel(logger: Logger, logLevel: Level) {
    (logger as LoggerImplJs).level = logLevel
  }

  /**
   * Returns the directly configured log level for the logger.
   * Call [getEffectiveLogLevel] in most cases instead.
   */
  fun getSpecificLogLevel(logger: Logger): Level? {
    return (logger as LoggerImplJs).level
  }

  /**
   * Returns the effective log level.
   * Will return the root level if no level is configured for the logger itself
   */
  fun getEffectiveLogLevel(logger: Logger): Level {
    return (logger as LoggerImplJs).level ?: rootLevel
  }

  /**
   * Initializes the log configuration from local storage.
   * If no root level could be found in local storage, the provided [fallbackRootLevel] is configured
   */
  fun initializeFromLocalStorage(fallbackRootLevel: Level = Level.INFO) {
    rootLevel = LoggerLocalStorage.readRootLevel() ?: fallbackRootLevel

    LoggerLocalStorage.readLoggerLevels { loggerName: String, level: Level ->
      setLogLevel(LoggerFactory.getLogger(loggerName), level)
    }
  }

  /**
   * Saves the current configuration to local storage
   */
  fun saveConfigurationToLocalStorage() {
    LoggerLocalStorage.storeRootLevel(rootLevel)

    LoggerFactory.cachedInstances().forEach { (loggerName, logger) ->
      val level = getSpecificLogLevel(logger)
      if (level != null) {
        LoggerLocalStorage.storeLoggerLevel(loggerName, level)
      }
    }
  }
}
