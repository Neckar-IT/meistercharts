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

import it.neckar.open.lang.orNull
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
class ConfiguringSupport<T: Any, A : Annotation>(
  /**
   * The type of the stored object - used for the store retrieval
   */
  private val storedObjectType: Class<out T>,
  /**
   * The annotation type that is used to fetch configuration values
   */
  private val annotationType: Class<A>,
  /**
   * The key that is used to store the original value in the store
   */
  private val key: String,
  /**
   * Contains logic to extract and apply the values
   */
  private val configuringStrategy: ConfiguringStrategy<T, A>,
) {

  /**
   * Returns the configured value - if there is an annotation present, and extract returned a value.
   * Else returns null.
   */
  fun getConfiguredValue(context: ExtensionContext): T? {
    val map = context.element
      .flatMap { annotatedElement -> Optional.ofNullable(annotatedElement.getAnnotation(annotationType)) }
      .map {
        configuringStrategy.extract(it)
      }
    return map.orNull()
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


  /**
   * Common before method that is called from [beforeAll] and [beforeEach]
   */
  private fun before(context: ExtensionContext, scope: Scope) {
    val configuredValue = getConfiguredValue(context) ?: return

    val originalValue = configuringStrategy.getOriginalValue()
    context.getStore(ExtensionContext.Namespace.GLOBAL).put(createStoreKey(scope), originalValue)

    //Apply the configured value
    configuringStrategy.applyValue(configuredValue)
  }

  /**
   * Common after method that is called from [afterAll] and [afterEach] - restores the original value
   */
  private fun after(@Nonnull context: ExtensionContext, @Nonnull scope: Scope) {
    val store = context.getStore(ExtensionContext.Namespace.GLOBAL)
    val originalValue = store[createStoreKey(scope), storedObjectType] ?: return

    //Restore the original value
    configuringStrategy.applyValue(originalValue)
  }

  /**
   * Creates the store key for the given scope
   */
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
