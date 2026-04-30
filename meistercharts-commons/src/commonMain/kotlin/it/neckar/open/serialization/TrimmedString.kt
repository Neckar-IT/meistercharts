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
@file:OptIn(ExperimentalSerializationApi::class)

package it.neckar.open.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * String type alias that trims whitespace from incoming values during deserialization
 * (ADL 0145 — Backend-Normalisierung von User-Input).
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class Customer(
 *   val firstName: TrimmedString,
 * )
 * ```
 *
 * Behavior:
 * - Decode of `"  Anna  "` produces `"Anna"`
 * - Decode of `"   "` produces `""` (empty allowed)
 * - Encode is unchanged — values in memory are expected to be already trimmed
 *
 * Trimming happens at the serialization boundary (REST, MongoDB), not at in-code
 * construction. Generators and tests are not user input and are not affected.
 *
 * For values that must not be blank after trimming, use [TrimmedStringNonBlank].
 * For nullable values where blank-after-trim should collapse to `null`, use [TrimmedStringOrNull].
 */
typealias TrimmedString = @Serializable(with = TrimmedStringSerializer::class) String

/**
 * Like [TrimmedString], but throws [IllegalArgumentException] when the trimmed value
 * is empty. Use for properties that semantically must not be blank.
 *
 * Decode of `"   "` throws.
 */
typealias TrimmedStringNonBlank = @Serializable(with = TrimmedStringNonBlankSerializer::class) String

/**
 * Nullable variant of [TrimmedString]. Trims whitespace and collapses blank values to `null`.
 *
 * - Decode of `"  Anna  "` produces `"Anna"`
 * - Decode of `"   "` produces `null`
 * - Decode of JSON `null` stays `null`
 */
typealias TrimmedStringOrNull = @Serializable(with = TrimmedStringOrNullSerializer::class) String?

/**
 * Serializer for [TrimmedString]. Trims on decode; encode is unchanged.
 */
object TrimmedStringSerializer : KSerializer<String> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("TrimmedString", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: String) {
    encoder.encodeString(value)
  }

  override fun deserialize(decoder: Decoder): String {
    return decoder.decodeString().trim()
  }
}

/**
 * Serializer for [TrimmedStringNonBlank]. Trims on decode and rejects blank values.
 */
object TrimmedStringNonBlankSerializer : KSerializer<String> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("TrimmedStringNonBlank", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: String) {
    encoder.encodeString(value)
  }

  override fun deserialize(decoder: Decoder): String {
    val trimmed = decoder.decodeString().trim()
    require(trimmed.isNotEmpty()) {
      "Trimmed string must not be blank"
    }
    return trimmed
  }
}

/**
 * Serializer for [TrimmedStringOrNull]. Trims on decode; blank-after-trim becomes `null`.
 */
object TrimmedStringOrNullSerializer : KSerializer<String?> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("TrimmedStringOrNull", PrimitiveKind.STRING).nullable

  override fun serialize(encoder: Encoder, value: String?) {
    if (value == null) {
      encoder.encodeNull()
    } else {
      encoder.encodeString(value)
    }
  }

  override fun deserialize(decoder: Decoder): String? {
    if (decoder.decodeNotNullMark().not()) {
      decoder.decodeNull()
      return null
    }
    val trimmed = decoder.decodeString().trim()
    return trimmed.ifEmpty { null }
  }
}
