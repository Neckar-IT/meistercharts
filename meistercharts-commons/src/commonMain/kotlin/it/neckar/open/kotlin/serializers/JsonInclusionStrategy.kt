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
package it.neckar.open.kotlin.serializers

import kotlinx.serialization.json.Json

/**
 * Defines how values are included when serializing objects to JSON.
 *
 * This controls whether properties with default values or explicit `null`s are written.
 */
enum class JsonInclusionStrategy(
  /**
   * If `true`, properties with default values are encoded.
   */
  val encodeDefaults: Boolean,
  /**
   * If `true`, properties with value `null` are explicitly included in the JSON output.
   */
  val explicitNulls: Boolean,
) {
  /**
   * Encode only non-default properties but include explicit `null`s.
   *
   * This is the default behavior for `kotlinx.serialization.Json` [kotlinx.serialization.json.JsonConfiguration].
   */
  SkipDefaultsIncludeNulls(
    encodeDefaults = false,
    explicitNulls = true,
  ),

  /**
   * Encode all properties, including defaults, but omit explicit `null`s - event for defaults.
   */
  EncodeDefaultsSkipNulls(
    encodeDefaults = true,
    explicitNulls = false,
  ),

  /**
   * Encode all properties, including defaults and explicit `null`s.
   */
  EncodeDefaultsIncludeNulls(
    encodeDefaults = true,
    explicitNulls = true,
  ),

  /**
   * Encode only non-default properties, omit explicit `null`s.
   */
  SkipDefaultsSkipNulls(
    encodeDefaults = false,
    explicitNulls = false,
  ),
  ;

  /**
   * Creates a [Json] instance with the current inclusion strategy
   */
  fun json(prettyPrint: Boolean = true, prettyPrintIndent: String = "  "): Json {
    return Json {
      this.prettyPrint = prettyPrint
      this.prettyPrintIndent = prettyPrintIndent
      this.encodeDefaults = this@JsonInclusionStrategy.encodeDefaults
      this.explicitNulls = this@JsonInclusionStrategy.explicitNulls
    }
  }

  companion object {
    /**
     * The default for `kotlinx.serialization`
     */
    val Default: JsonInclusionStrategy = SkipDefaultsIncludeNulls
  }
}
