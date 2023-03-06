/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.demo

import it.neckar.open.javafx.EnumTranslator
import it.neckar.open.javafx.EnumTranslatorUtil
import it.neckar.logging.LoggerFactory
import org.jetbrains.annotations.PropertyKey
import java.text.MessageFormat
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

/**
 * Offers access to the demo texts
 */
object DemoMessages {
  internal const val BUNDLE_NAME = "com.meistercharts.demo.demo-texts"

  private val resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault(), DemoMessages::class.java.classLoader)

  /**
   * Returns the value for the given key
   */
  operator fun get(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String): String {
    try {
      return resourceBundle.getString(key)
    } catch (ignore: MissingResourceException) {
      logger.error("Key not found <" + resourceBundle.javaClass.name + ": " + key + ">")
      return key
    }
  }

  /**
   * Formats the resolved value for the given key
   */
  operator fun get(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String, vararg args: Any?): String {
    val message = get(key)
    return MessageFormat.format(message, *args)
  }

  /**
   * Returns the value or null if the key is null
   *
   * @param key the key
   */
  fun getNullable(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String?): String? {
    return if (key == null) {
      null
    } else get(key)
  }

  operator fun get(enumValue: Enum<*>, specifier: String? = null): String {
    return get(getKey(enumValue, specifier))
  }

  private fun getKey(enumValue: Enum<*>): String {
    return getKey(enumValue, null)
  }

  /**
   * Returns the key for a enum value and specifier
   */
  fun getKey(enumValue: Enum<*>, specifier: String?): String {
    return if (specifier == null) {
      enumValue.name
    } else enumValue.name + "." + specifier

  }

  /**
   * Registers the enum translator with the demo messages as sources
   */
  fun registerEnumTranslator() {
    EnumTranslatorUtil.addEnumTranslator(object : EnumTranslator {
      override fun translate(item: Enum<*>): String {
        return DemoMessages[item]
      }
    })
  }

  private val logger = LoggerFactory.getLogger("com.meistercharts.demo.DemoMessages")
}
