package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.support.AnnotationSupport

/**
 * Enables logging for tests (overrides [DisableLoggingCondition]).
 *
 * Reads the log level from the [EnableLogging] annotation.
 */
class EnableLoggingCondition : BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

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
   * Finds the log level from the [EnableLogging] annotation.
   * Checks the test method first, then the test class.
   */
  private fun findLogLevel(context: ExtensionContext): org.slf4j.event.Level {
    // Check method-level annotation first
    val methodAnnotation = context.testMethod
      .flatMap { AnnotationSupport.findAnnotation(it, EnableLogging::class.java) }

    if (methodAnnotation.isPresent) {
      val level: org.slf4j.event.Level = methodAnnotation.get().level
      return level
    }

    // Fall back to class-level annotation
    val classAnnotation = context.testClass
      .flatMap { AnnotationSupport.findAnnotation(it, EnableLogging::class.java) }

    if (classAnnotation.isPresent) {
      return classAnnotation.get().level
    }

    // Default to INFO if no annotation found
    return org.slf4j.event.Level.INFO
  }
}
