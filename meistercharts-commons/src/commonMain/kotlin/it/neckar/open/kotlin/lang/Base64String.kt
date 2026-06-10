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
package it.neckar.open.kotlin.lang

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * A Base64-encoded string. Backed by [String], so equality and serialization are structural — it
 * serializes as a plain string (JSON, BSON, …).
 *
 * Use this instead of a raw `String` whenever a value holds Base64-encoded bytes, so the encoding is
 * part of the type and the [decode]/[of] conversions live in one place.
 */
@JvmInline
@Serializable
value class Base64String(override val value: String) : ValueClassString {
  /**
   * Decodes this Base64 string to its raw bytes.
   */
  fun decode(): ByteArray = value.fromBase64()

  companion object {
    /**
     * Base64-encodes the given [bytes].
     */
    fun of(bytes: ByteArray): Base64String = Base64String(bytes.toBase64())
  }
}
