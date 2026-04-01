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

import it.neckar.open.annotations.JavaFriendly
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig

/**
 * Formats a value adding an offset to it
 */
class OffsetFormat(
  /**
   * The delegate that is used to format the offset value
   */
  val delegate: CachedNumberFormat,
  /**
   * Provides the offset that is added to the value before formatting using the delegate
   */
  val offsetProvider: () -> Double
) : CachedNumberFormat {
  @JavaFriendly
  constructor(
    delegate: CachedNumberFormat,
    offsetProvider: OffsetProvider
  ) : this(delegate, offsetProvider::offset)

  override val currentCacheSize: Int
    get() = delegate.currentCacheSize

  override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val offset = offsetProvider()
    return delegate.format(value + offset, i18nConfiguration, whitespaceConfig)
  }

  override val precision: Double
    get() = delegate.precision
}

/**
 * Provides the offset
 */
@JavaFriendly
interface OffsetProvider {
  /**
   * Returns the offset
   */
  val offset: Double
}
