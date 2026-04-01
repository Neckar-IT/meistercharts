/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
abstract class AbstractResourceProvidingExtension<T : Any>(
  /**
   * The resource type that is provided by this extension
   */
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
   * Creates a resource from the context
   */
  @Suppress("UNCHECKED_CAST")
  protected fun getResource(extensionContext: ExtensionContext, key: Member): T {
    val map: MutableMap<Member, T> = getStore(extensionContext)
      .computeIfAbsent(
        extensionContext.testClass.get(), { ConcurrentHashMap<Member, T>() },
        MutableMap::class.java as Class<MutableMap<Member, T>>
      )

    return map.computeIfAbsent(key) { _: Member? -> createResource(extensionContext) }
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
   * This method is called for each method parameter. It must convert the resource to the required parameter type.
   * The converted values are then passed to the test method.
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
