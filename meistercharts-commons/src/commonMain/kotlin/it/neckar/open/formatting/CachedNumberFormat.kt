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
package it.neckar.open.formatting

import it.neckar.open.collections.cache
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig

/**
 * A format that returns cached values.
 *
 * This is a tagging interface to ensure a cached format is used
 *
 */
interface CachedNumberFormat : NumberFormat {
  /**
   * Returns the current cache size
   */
  val currentCacheSize: Int
}

/**
 * A format that caches the results
 */
class DefaultCachedFormat internal constructor(
  val format: NumberFormat,
  /**
   * The maximum size of the cache
   */
  val cacheSize: Int = 500,

  /**
   * The hash function that is used to calculate the hash for the current value.
   * This method can *also* use external variables (e.g. a locale or another configuration).
   */
  val hashFunction: (value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig) -> Int = defaultHashFunction,

  ) : CachedNumberFormat {

  init {
    require(format !is CachedNumberFormat) { "cannot cache an already cached number format" }
  }

  /**
   * The cache for the "normal" formatted strings
   */
  private val formatCache = cache<Int, String>("DefaultCachedFormat", cacheSize)

  /**
   * Returns the size of the cache
   */
  override val currentCacheSize: Int
    get() = formatCache.size

  override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    //Calculate the hash code to avoid instantiation of objects
    val key = hashFunction(value, i18nConfiguration, whitespaceConfig)

    //Do *NOT* use #getOrStore: its capturing lambda would allocate on every call - even on cache hits.
    //This format sits on the per-frame hot path (axis ticks, labels), where the hit path must be allocation-free.
    val found = formatCache[key]
    if (found != null) {
      return found
    }

    val formatted = format.format(value, i18nConfiguration, whitespaceConfig)
    formatCache.store(key, formatted)
    return formatted
  }

  override val precision: Double
    get() = format.precision

  companion object {
    /**
     * The hash function that is used per default
     */
    val defaultHashFunction: (value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig) -> Int = { value, i18nConfiguration, spaceConfig ->
      31 * value.hashCode() + i18nConfiguration.hashCode() + spaceConfig.hashCode()
    }
  }
}

/**
 * Caches the results of the number format
 */
fun NumberFormat.cached(
  cacheSize: Int = 500,
  /**
   * Calculates an additional hash that will be added to the hash of the value.
   * Can be used to create a unique hash for other (external) factors: For example a unit
   */
  additionalHashFunction: (() -> Int)? = null
): CachedNumberFormat {
  //Create a custom hash function - only if a additionalHashFunction is provided
  val hashFunction = additionalHashFunction?.let {
    //Create a custom hash function
    { value, i18nConfiguration, spaceConfig ->
      DefaultCachedFormat.defaultHashFunction(value, i18nConfiguration, spaceConfig) + additionalHashFunction()
    }
  } ?: DefaultCachedFormat.defaultHashFunction //Fallback to the default hash function

  return DefaultCachedFormat(this, cacheSize = cacheSize, hashFunction = hashFunction)
}

/**
 * Helper method to avoid unnecessary calls to cached
 */
@Suppress("UNUSED_PARAMETER")
@Deprecated("Do not cache a cached format", ReplaceWith("this"), level = DeprecationLevel.ERROR)
fun CachedNumberFormat.cached(
  cacheSize: Int = 500,
  hashFunction: (value: Double, i18nConfiguration: I18nConfiguration) -> Int = { _, _ -> throw UnsupportedOperationException("must not be called!") }
): CachedNumberFormat {
  return this
}

