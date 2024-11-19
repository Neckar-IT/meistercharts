package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ExtensionContext
import java.util.Optional
import javax.annotation.Nonnull

/**
 * Configuration support that can be used by extensions to configure unit tests.
 *
 * Extensions using this support should:
 *
 * * Implement `BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback`
 *
 *
 * delegate four method calls:
 * ```
 * override fun beforeAll(extensionContext: ExtensionContext) {
 *   configuringSupport.beforeAll(extensionContext)
 * }
 *
 * override fun afterAll(extensionContext: ExtensionContext) {
 *   configuringSupport.afterAll(extensionContext)
 * }
 *
 * override fun beforeEach(extensionContext: ExtensionContext) {
 *   configuringSupport.beforeEach(extensionContext)
 * }
 *
 * override fun afterEach(extensionContext: ExtensionContext) {
 *   configuringSupport.afterEach(extensionContext)
 * }
 * ```
 */
class ConfiguringSupport<T, A : Annotation>(
  private val storedObjectType: Class<T>,
  private val annotationType: Class<A>,
  private val key: String,
  private val callback: ConfigurationCallback<T, A>
) {

  /**
   * Returns the configured value - if there is an annotation present, and extract returned a value
   */
  fun getConfiguredValue(context: ExtensionContext): Optional<T> {
    return context.element
      .flatMap { annotatedElement -> Optional.ofNullable(annotatedElement.getAnnotation(annotationType)) }
      .map {
        callback.extract(it)
      }
  }

  /**
   * Should be called from the extension
   */
  fun beforeAll(extensionContext: ExtensionContext) {
    before(extensionContext, Scope.CLASS)
  }

  /**
   * Should be called from the extension
   */
  fun beforeEach(extensionContext: ExtensionContext) {
    before(extensionContext, Scope.METHOD)
  }

  /**
   * Should be called from the extension
   */
  fun afterAll(extensionContext: ExtensionContext) {
    after(extensionContext, Scope.CLASS)
  }

  /**
   * Should be called from the extension
   */
  fun afterEach(extensionContext: ExtensionContext) {
    after(extensionContext, Scope.METHOD)
  }


  private fun before(context: ExtensionContext, scope: Scope) {
    val configuredValue = getConfiguredValue(context).orElse(null) ?: return

    val originalValue = callback.getOriginalValue()
    context.getStore(ExtensionContext.Namespace.GLOBAL).put(createStoreKey(scope), originalValue)
    callback.applyValue(configuredValue)
  }

  private fun after(@Nonnull context: ExtensionContext, @Nonnull scope: Scope) {
    context.getStore(ExtensionContext.Namespace.GLOBAL)[createStoreKey(scope), storedObjectType]?.let { callback.applyValue(it) }
  }

  private fun createStoreKey(scope: Scope): String {
    return "${scope.name}.$key"
  }

  /**
   * The scope for the store
   */
  enum class Scope {
    CLASS,
    METHOD
  }
}
