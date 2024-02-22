package it.neckar.open.test.utils

import it.neckar.open.kotlin.lang.requireNotNull
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Disables logging for tests
 */
class DisableLoggingCondition : BeforeEachCallback, AfterEachCallback {
  var originalLevels: LoggingTestSupport.OriginalLevels? = null

  override fun beforeEach(context: ExtensionContext?) {
    originalLevels = LoggingTestSupport.disableLogging()
  }

  override fun afterEach(context: ExtensionContext?) {
    LoggingTestSupport.enableLogging(originalLevels.requireNotNull())
  }
}
