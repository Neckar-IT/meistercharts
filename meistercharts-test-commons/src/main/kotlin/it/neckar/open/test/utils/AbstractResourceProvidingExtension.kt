package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import java.lang.reflect.Member
import java.lang.reflect.Parameter
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.Nonnull

/**
 * Abstract base class for extensions that provide a resource
 *
 */
abstract class AbstractResourceProvidingExtension<T>(
  val resourceType: Class<T>,
) : ParameterResolver, AfterTestExecutionCallback, TestInstancePostProcessor {

  override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
    for (field in testInstance.javaClass.declaredFields) {
      if (resourceType.isAssignableFrom(field.type)) {
        val resource = getResource(context, field)
        field.isAccessible = true
        field[testInstance] = resource
      }
    }
  }

  /**
   * Creates a resource
   */
  @Suppress("UNCHECKED_CAST")
  protected fun getResource(extensionContext: ExtensionContext, key: Member): T {
    val map: MutableMap<Member, T> = getStore(extensionContext)
      .getOrComputeIfAbsent(
        extensionContext.testClass.get(), { ConcurrentHashMap<Member, T>() },
        MutableMap::class.java as Class<MutableMap<Member, T>>
      ) as MutableMap<Member, T>

    return map.computeIfAbsent(key) { member: Member? -> createResource(extensionContext) }
  }

  /**
   * Creates the resource
   */
  protected abstract fun createResource(extensionContext: ExtensionContext): T

  @Throws(ParameterResolutionException::class)
  override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
    return try {
      val resource = getResource(extensionContext, extensionContext.testMethod.orElseThrow { IllegalStateException("No test method found") })
      val parameter = parameterContext.parameter
      if (resourceType.isAssignableFrom(parameter.type)) {
        //return the resource directly
        resource
      } else convertResourceForParameter(parameter, resource)
    } catch (e: Exception) {
      throw ParameterResolutionException("failed to create resource", e)
    }
  }

  /**
   * Converts the given resource to an object for the parameter - based on the type and annotations of the parameter
   */
  protected abstract fun convertResourceForParameter(parameter: Parameter, resource: T): Any

  override fun afterTestExecution(context: ExtensionContext) {
    // clean up test instance
    cleanupResources(context)
    if (context.parent.isPresent) {
      // clean up injected member
      cleanupResources(context.parent.get())
    }
  }

  protected fun cleanupResources(@Nonnull extensionContext: ExtensionContext) {
    for (resource in getResources(extensionContext)) {
      cleanup(resource)
    }
  }

  /**
   * Callback to clean up the given resource
   */
  protected abstract fun cleanup(@Nonnull resource: T)

  @Suppress("UNCHECKED_CAST")
  protected fun getResources(extensionContext: ExtensionContext): Iterable<T> {
    val map = getStore(extensionContext).get<Map<*, T>>(extensionContext.testClass.get(), MutableMap::class.java as Class<Map<*, T>>) ?: return emptySet()
    return map.values
  }

  @Throws(ParameterResolutionException::class)
  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
    return resourceType.isAssignableFrom(parameterContext.parameter.type)
  }

  /**
   * Returns the store for this class and context
   */
  protected fun getStore(context: ExtensionContext): ExtensionContext.Store {
    return context.getStore(ExtensionContext.Namespace.create(javaClass, context))
  }
}
