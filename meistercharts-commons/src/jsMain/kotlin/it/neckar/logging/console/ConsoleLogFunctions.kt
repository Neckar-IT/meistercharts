package it.neckar.logging.console

import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer
import it.neckar.logging.LoggerFactory
import it.neckar.logging.LoggerLocalStorage
import it.neckar.logging.LoggerName
import it.neckar.logging.ShortenedLoggerName
import it.neckar.open.collections.fastForEach


/**
 * Is registered at the window object and provides some functions to interact with the logging framework
 */
@JsExport
data class ConsoleLogFunctions(val prefix: String) {
  override fun toString(): String {
    return "The Console functions!"
  }

  fun help() {
    println(
      """
      |Available functions:
      | * $prefix.rootLevel: Returns the current root level
      | * $prefix.rootLevel=newLevel: Sets the root level to the given value (supports "INFO", "DEBUG", "WARN", "ERROR")
      | * $prefix.list(): Lists all known loggers and their level
      | * $prefix.get(loggerName): Returns the log level for the provided logger
      | * $prefix.set(loggerName, newLevel): Sets the log level for the provided logger (supports "INFO", "DEBUG", "WARN", "ERROR")
      | * $prefix.localStorage.help(): Prints the help related to the local storage
      |""".trimMargin()
    )
  }

  /**
   * Offers access to local storage
   */
  @Suppress("unused")
  val localStorage: LocalStorageFunctions = LocalStorageFunctions("$prefix.localStorage")

  @Suppress("unused")
  var rootLevel: String
    get() = LogConfigurer.rootLevel.name
    set(value) {
      guessLevel(value).let {
        LogConfigurer.setRootLoggerLevel(it)
        LoggerLocalStorage.storeRootLevel(LogConfigurer.rootLevel)
      }
    }

  private fun guessLevel(value: String?): Level {
    return Level.guess(value) ?: throw IllegalArgumentException("Invalid log level: $value. Supported values: ${Level.entries.joinToString(", ")}")
  }

  /**
   * Returns the log level for a specific logger
   */
  operator fun get(loggerName: String): String {
    val logger = LoggerFactory.getLoggerOrNull(LoggerName(loggerName))
    if (logger == null) {
      println("Logger $loggerName not found")
      return "NOT FOUND"
    }

    val level = LogConfigurer.getSpecificLogLevel(logger)
    val effectiveLevel = LogConfigurer.getEffectiveLogLevel(logger)
    println("\t${logger.name} [${level?.name ?: "-"}] - ${effectiveLevel.name}")

    return effectiveLevel.name
  }

  /**
   * Sets the log level. Also supports the short logger name
   */
  operator fun set(loggerName: String, logLevel: String?): String {
    val level = guessLevel(logLevel)

    //Try for a perfect hit first
    val exactHit = LoggerFactory.getLoggerOrNull(LoggerName(loggerName))
    if (exactHit != null) {
      console.log("Set log level for $loggerName to $level")
      LogConfigurer.setLogLevel(exactHit, level)
      LoggerLocalStorage.storeLoggerLevel(exactHit, level)
      return level.name
    }

    val byShortened = LoggerFactory.findLoggerByShortenedName(ShortenedLoggerName(loggerName))
    if (byShortened.isEmpty()) {
      console.log("Set log level for $loggerName to $level (currently unknown logger)")
      val logger = LoggerFactory.getLogger(loggerName)
      LogConfigurer.setLogLevel(logger, level)
      LoggerLocalStorage.storeLoggerLevel(logger, level)
    }

    byShortened.fastForEach { logger ->
      console.log("Set log level for $loggerName to $level (matches shortened name)")
      LogConfigurer.setLogLevel(logger, level)
      LoggerLocalStorage.storeLoggerLevel(logger, level)
    }

    return level.name
  }

  /**
   * List all loggers and their level
   */
  fun list(): Any? {
    println("Root log level: ${LogConfigurer.rootLevel.name}")

    println("Currently known logger instances:")

    LoggerFactory.cachedInstances().values
      .sortedBy { it.name }
      .forEach { logger ->
        val level: Level? = LogConfigurer.getSpecificLogLevel(logger)
        val effectiveLevel: Level = LogConfigurer.getEffectiveLogLevel(logger)
        println("\t${logger.name} [${level?.name ?: "-"}] - ${effectiveLevel.name}")
    }

    return null
  }
}
