/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.logging.console

import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer
import it.neckar.logging.LoggerFactory
import it.neckar.logging.LoggerLocalStorage
import it.neckar.logging.LoggerName
import it.neckar.logging.ShortenedLoggerName
import it.neckar.open.collections.fastForEach
import it.neckar.open.version.VersionInformation


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
      | * $prefix.version(): Prints the version information
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

  fun version(): Any? {
    println("Build Date: ${VersionInformation.buildDate}")
    println("Git Commit: ${VersionInformation.gitHash}")
    return null
  }
}
