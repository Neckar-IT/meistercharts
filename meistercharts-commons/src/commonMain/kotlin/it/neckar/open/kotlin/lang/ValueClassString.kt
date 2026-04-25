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

/**
 * Tagging interface for inline value classes that wrap a single String.
 * Implementing classes *must* be value classes with a primitive-String serializer
 * so the underlying storage representation is a plain string (JSON, BSON, etc.).
 *
 * Example:
 * ```kotlin
 * @Serializable(with = MyId.Serializer::class)
 * @JvmInline
 * value class MyId(override val value: String) : ValueClassString
 * ```
 *
 * Consumers can use the tag to write generic, type-safe APIs that accept any
 * string-backed value class — e.g. MongoDB regex filters or Ktor path parameters
 * — without resorting to `.value` unwraps or `.name` field-string lookups at call
 * sites.
 */
interface ValueClassString {
  /**
   * The underlying string representation.
   */
  val value: String
}
