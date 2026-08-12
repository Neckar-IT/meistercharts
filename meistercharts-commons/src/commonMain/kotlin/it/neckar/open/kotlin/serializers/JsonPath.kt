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

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * The keys to follow into a nested JSON object, in order. The empty path addresses the root.
 *
 * Every string is a valid key - including the empty one - therefore no invariant beyond the order.
 */
@JvmInline
@Serializable
value class JsonPath(val keys: List<String>) {
  constructor(vararg keys: String) : this(keys.toList())

  val isRoot: Boolean
    get() = keys.isEmpty()

  operator fun plus(key: String): JsonPath = JsonPath(keys + key)

  /**
   * The dot notation - for messages, not for parsing: a key may itself contain a dot.
   */
  override fun toString(): String = keys.joinToString(".")

  companion object {
    val Root: JsonPath = JsonPath(emptyList())

    /**
     * Splits a dot-separated string into a path. Only correct as long as no key contains a dot.
     */
    fun parse(dotSeparated: String): JsonPath = JsonPath(dotSeparated.split("."))
  }
}
