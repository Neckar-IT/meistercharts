package it.neckar.open.test.utils

import ch.qos.logback.classic.Level
import it.neckar.commons.logback.toLogback
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.support.AnnotationSupport

/**
 * Disables logging for tests.
 *
 * Reads the log level from the [DisableLogging] annotation.
 */
class DisableLoggingCondition : BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

  var originalLevelsAll: LoggingTestSupport.OriginalLevels? = null

  override fun beforeAll(context: ExtensionContext) {
    val level = findLogLevel(context)
    originalLevelsAll = LoggingTestSupport.forceLoggingLevel(level = level)
  }

  override fun afterAll(context: ExtensionContext) {
    originalLevelsAll?.let {
      LoggingTestSupport.enableLogging(it)
    }
  }

  var originalLevelsEach: LoggingTestSupport.OriginalLevels? = null

  override fun beforeEach(context: ExtensionContext) {
    val level = findLogLevel(context)
    originalLevelsEach = LoggingTestSupport.forceLoggingLevel(level = level)
  }

  override fun afterEach(context: ExtensionContext) {
    originalLevelsEach?.let {
      LoggingTestSupport.enableLogging(it)
    }
  }

  /**
   * Finds the log level from the [DisableLogging] annotation.
   * Checks the test method first, then the test class.
   * If [DisableLogging.mute] is true, returns [Level.OFF].
   */
  private fun findLogLevel(context: ExtensionContext): Level {
    val annotation = context.testMethod
      .flatMap { AnnotationSupport.findAnnotation(it, DisableLogging::class.java) }
      .or { context.testClass.flatMap { AnnotationSupport.findAnnotation(it, DisableLogging::class.java) } }

    if (annotation.isPresent) {
      val disableLogging = annotation.get()
      if (disableLogging.mute) {
        return Level.OFF
      }
      return disableLogging.level.toLogback()
    }

    return Level.ERROR
  }
}
