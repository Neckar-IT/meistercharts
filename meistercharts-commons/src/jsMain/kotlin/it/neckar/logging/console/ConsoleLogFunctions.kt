package it.neckar.logging.console

import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer
import it.neckar.logging.LoggerFactory
import it.neckar.logging.LoggerLocalStorage
import it.neckar.logging.LoggerLocalStorageKeys
import kotlinx.browser.window


object ConsoleLogFunctionsSupport {

  /**
   * Registers the console log functions at the window object
   */
  fun init(name: String) {
    console.log("Initializing console log functions for $name")
    window.asDynamic()[name] = it.neckar.logging.console.ConsoleLogFunctions(name)
  }
}

@JsExport
class LocalStorageFunctions(val prefix: String) {
  fun help() {
    println(
      """
      |Available functions:
      | * ${prefix}.list(): Lists the local storage configuration
      | * ${prefix}.clear(): Clears the local storage configuration for logs
      """.trimIndent()
    )
  }

  fun list() {
    println(
      """
      |Local storage configuration:
      | * rootLevel: ${LoggerLocalStorageKeys.RootLevel}
    """.trimIndent()
    )
  }

  fun clear() {
    window.localStorage.removeItem(LoggerLocalStorageKeys.RootLevel)
  }
}

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
    val logger = LoggerFactory.getLoggerOrNull(loggerName)
    if (logger == null) {
      println("Logger $loggerName not found")
      return "NOT FOUND"
    }

    val level = LogConfigurer.getSpecificLogLevel(logger)
    val effectiveLevel = LogConfigurer.getEffectiveLogLevel(logger)
    println("\t${logger.name} [${level?.name ?: "-"}] - ${effectiveLevel.name}")

    return effectiveLevel.name
  }

  operator fun set(loggerName: String, logLevel: String?): String {
    val logger = LoggerFactory.getLogger(loggerName)

    val level = guessLevel(logLevel)
    LogConfigurer.setLogLevel(logger, level)

    LoggerLocalStorage.storeLoggerLevel(logger, level)

    console.log("Set log level for $loggerName to $level")
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
