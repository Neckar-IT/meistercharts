package it.neckar.open.test.utils

import it.neckar.open.time.NowProvider
import it.neckar.open.time.VirtualNowProvider
import it.neckar.open.time.nowProvider
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import java.lang.reflect.Parameter
import javax.annotation.Nonnull

/**
 * Extension that provides the virtual now provider as parameter.
 * Use [WithVirtualTime] at the class/method and add [VirtualNowProvider] to the test method parameters
 */
class VirtualNowProviderExtension : AbstractResourceProvidingExtension<VirtualNowProvider>(VirtualNowProvider::class.java), BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

  private val configuringSupport: ConfiguringSupport<NowProvider, WithVirtualTime> = ConfiguringSupport(
    storedObjectType = NowProvider::class.java,
    annotationType = WithVirtualTime::class.java,
    key = "virtualTime",
    configuringStrategy = object : ConfiguringStrategy<NowProvider, WithVirtualTime> {
      override fun getOriginalValue(): NowProvider {
        return nowProvider
      }

      override fun extract(annotation: WithVirtualTime): NowProvider {
        return VirtualNowProvider(annotation.value)
      }

      override fun applyValue(value: NowProvider) {
        nowProvider = value
      }
    }
  )


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

  override fun createResource(extensionContext: ExtensionContext): VirtualNowProvider {
    return nowProvider as? VirtualNowProvider ?: throw IllegalStateException("Invalid instance of nowProvider. Expected <VirtualNowProvider> but was <$nowProvider>")
  }

  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
    if (super.supportsParameter(parameterContext, extensionContext)) {
      return true
    }

    return parameterContext.parameter.isAnnotationPresent(WithVirtualTime::class.java)
  }

  override fun convertResourceForParameter(@Nonnull parameter: Parameter, @Nonnull resource: VirtualNowProvider): Any {
    return resource
  }

  override fun cleanup(@Nonnull resource: VirtualNowProvider) {
  }
}
