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
