package it.neckar.open.test.utils.chrome

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * JUnit 5 Extension that kills all Chrome processes before each test.
 */
class KillChromeExtension : BeforeEachCallback {
  override fun beforeEach(context: ExtensionContext) {
    ChromeProcessKiller.killAllChromeProcesses()
  }
}
