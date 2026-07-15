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
import kotlin.jvm.JvmOverloads

/**
 * A format that returns cached values.
 *
 * This interface should be used at declarations (e.g. in Styles) to ensure a cache is used
 *
 */
interface CachedDateTimeFormat : DateTimeFormat {
  /**
   * Returns the current cache size
   */
  val currentCacheSize: Int
}

/**
 * A format that caches the results
 */
class DefaultCachedDateTimeFormat @JvmOverloads constructor(
  val format: DateTimeFormat,
  /**
   * The maximum size of the cache
   */
  val cacheSize: Int = 500
) : CachedDateTimeFormat {

  init {
    require(format !is CachedDateTimeFormat) { "cannot cache an already cached dateTime format" }
  }

  /**
   * The cache for the "normal" formatted strings
   */
  private val formatCache = cache<Int, String>("DefaultCachedDateTimeFormat", cacheSize)

  /**
   * Returns the size of the cache
   */
  override val currentCacheSize: Int
    get() = formatCache.size

  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val key = 31 * timestamp.hashCode() + 17 * i18nConfiguration.hashCode() + 18 * whitespaceConfig.hashCode()

    return formatCache.getOrStore(key) {
      format.format(timestamp, i18nConfiguration, whitespaceConfig)
    }
  }
}

/**
 * Caches the results of the dateTime format
 */
fun DateTimeFormat.cached(cacheSize: Int = 100): CachedDateTimeFormat {
  return DefaultCachedDateTimeFormat(this, cacheSize = cacheSize)
}

