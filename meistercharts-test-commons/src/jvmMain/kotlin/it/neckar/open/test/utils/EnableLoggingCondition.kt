package it.neckar.open.test.utils

import ch.qos.logback.classic.Level
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Enables logging for tests (overrides [DisableLoggingCondition]).
 */
class EnableLoggingCondition : BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {
  var originalLevelsAll: LoggingTestSupport.OriginalLevels? = null
  override fun beforeAll(context: ExtensionContext) {
    originalLevelsAll = LoggingTestSupport.forceLoggingLevel(level = Level.INFO)
  }

  override fun afterAll(context: ExtensionContext) {
    originalLevelsAll?.let {
      LoggingTestSupport.enableLogging(it)
    }
  }

  var originalLevelsEach: LoggingTestSupport.OriginalLevels? = null

  override fun beforeEach(context: ExtensionContext) {
    originalLevelsEach = LoggingTestSupport.forceLoggingLevel(level = Level.INFO)
  }

  override fun afterEach(context: ExtensionContext) {
    originalLevelsEach?.let {
      LoggingTestSupport.enableLogging(it)
    }
  }
}
