package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Abstract base class for extensions that configure stuff and revert it after the tests
 *
 */
abstract class AbstractConfiguringExtension<T, A : Annotation> protected constructor(
  storedObjectType: Class<T>,
  enumType: Class<A>,
  key: String,
  /**
   * The callback
   */
  callback: ConfigurationCallback<T, A>
) : BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

  private val configuringSupport: ConfiguringSupport<T, A> = ConfiguringSupport(storedObjectType, enumType, key, callback)

  override fun beforeAll(extensionContext: ExtensionContext) {
    configuringSupport.beforeAll(extensionContext)
  }

  override fun afterAll(extensionContext: ExtensionContext) {
    configuringSupport.afterAll(extensionContext)
  }

  override fun beforeEach(extensionContext: ExtensionContext) {
    configuringSupport.beforeEach(extensionContext)
  }

  override fun afterEach(extensionContext: ExtensionContext) {
    configuringSupport.afterEach(extensionContext)
  }
}
