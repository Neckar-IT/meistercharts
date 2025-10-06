package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Disables logging for tests
 */
class DisableLoggingCondition : BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {
  var originalLevelsAll: LoggingTestSupport.OriginalLevels? = null

  override fun beforeAll(context: ExtensionContext) {
    originalLevelsAll = LoggingTestSupport.forceLoggingLevel()
  }

  override fun afterAll(context: ExtensionContext) {
    originalLevelsAll?.let {
      LoggingTestSupport.enableLogging(it)
    }
  }

  var originalLevelsEach: LoggingTestSupport.OriginalLevels? = null

  override fun beforeEach(context: ExtensionContext) {
    originalLevelsEach = LoggingTestSupport.forceLoggingLevel()
  }

  override fun afterEach(context: ExtensionContext) {
    originalLevelsEach?.let {
      LoggingTestSupport.enableLogging(it)
    }
  }
}
