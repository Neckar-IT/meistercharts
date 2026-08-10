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
package it.neckar.open.serialization

import assertk.*
import assertk.assertions.*
import it.neckar.open.kotlin.serializers.JsonInclusionStrategy
import it.neckar.open.test.utils.JsonUtils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

/**
 * Decodes the given JSON and hands the result together with [expected] to [comparisonCheck]. Returns the decoded instance.
 *
 * Serialization is not exercised — use [roundTrip] unless the JSON deliberately differs from what the encoder produces
 * (legacy payloads, hand-written samples, JSON from another system).
 */
fun <T> testDeserialization(
  serializer: KSerializer<T>,
  expected: T,
  inclusionStrategy: JsonInclusionStrategy = JsonInclusionStrategy.EncodeDefaultsIncludeNulls,
  comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  //language=JSON
  json: () -> String,
): T {
  val encoder = Json {
    defaultJsonConfiguration(true, inclusionStrategy)
  }

  return testDeserialization(encoder, serializer, json(), comparisonCheck, expected)
}

fun <T> testDeserialization(
  serializer: KSerializer<T>,
  expected: T,
  inclusionStrategy: JsonInclusionStrategy = JsonInclusionStrategy.EncodeDefaultsIncludeNulls,
  comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  //language=JSON
  json: String,
): T {
  return testDeserialization(serializer, expected, inclusionStrategy, comparisonCheck) { json }
}

/**
 * Encodes [objectToSerialize] and asserts the result equals the JSON from [json], comparing the parsed trees:
 * key order and formatting are irrelevant, array order is not. Returns the encoded JSON.
 */
@IgnorableReturnValue
inline fun <reified T> testSerialization(
  objectToSerialize: T,
  serializer: KSerializer<T> = serializer(),
  serializersModule: SerializersModule = EmptySerializersModule(),
  inclusionStrategy: JsonInclusionStrategy = JsonInclusionStrategy.EncodeDefaultsIncludeNulls,
  //language=JSON
  json: () -> String,
): String {
  val encoder = Json {
    this.serializersModule = serializersModule
    this.defaultJsonConfiguration(true, inclusionStrategy)
  }

  return testSerialization(objectToSerialize = objectToSerialize, encoder = encoder, serializer = serializer, json())
}

/**
 * Encodes [objectToSerialize], asserts the JSON equals [expectedJson], decodes it again and hands both instances to
 * [comparisonCheck]. A null [expectedJson] skips the JSON assertion, leaving only the round trip itself under test.
 */
@IgnorableReturnValue
inline fun <reified T> roundTrip(
  objectToSerialize: T,
  serializer: KSerializer<T>,
  serializersModule: SerializersModule = EmptySerializersModule(),
  inclusionStrategy: JsonInclusionStrategy = JsonInclusionStrategy.EncodeDefaultsIncludeNulls,
  noinline comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  expectedJson: String?,
): T {
  return roundTrip(objectToSerialize, serializer, serializersModule, inclusionStrategy, comparisonCheck) { expectedJson }
}

/**
 * Tests the round trip. If the [expectedJsonProvider] provides null, the resulting JSON will not be checked
 */
@IgnorableReturnValue
inline fun <reified T> roundTrip(
  objectToSerialize: T,
  serializer: KSerializer<T> = serializer(),
  serializersModule: SerializersModule = EmptySerializersModule(),
  inclusionStrategy: JsonInclusionStrategy = JsonInclusionStrategy.EncodeDefaultsIncludeNulls,
  noinline comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  //language=JSON
  noinline expectedJsonProvider: () -> String?,
): T {
  val encoder: Json = Json {
    this.serializersModule = serializersModule
    this.defaultJsonConfiguration(true, inclusionStrategy)
  }

  return roundTrip(objectToSerialize, serializer, encoder, comparisonCheck, expectedJsonProvider)
}

/**
 * Round trip against a pre-configured [encoder] instead of the default configuration — for tests that need a custom
 * [Json] instance (own serializers module, different inclusion strategy). A null [expectedJson] skips the JSON assertion.
 */
@IgnorableReturnValue
inline fun <reified T> roundTrip(
  objectToSerialize: T,
  serializer: KSerializer<T> = serializer(),
  encoder: Json,
  noinline comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  expectedJson: String?,
): T {
  return roundTrip(objectToSerialize, serializer, encoder, comparisonCheck) { expectedJson }
}

/**
 * Round trip against a pre-configured [encoder]. Returns the deserialized instance; a null value from
 * [expectedJsonProvider] skips the JSON assertion.
 */
@IgnorableReturnValue
inline fun <reified T> roundTrip(
  objectToSerialize: T,
  serializer: KSerializer<T> = serializer(),
  encoder: Json,
  /**
   * Comparison check that is called. Should throw an exception
   */
  noinline comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  expectedJsonProvider: () -> String?,
): T {
  return _roundTrip(encoder, serializer, objectToSerialize, comparisonCheck, expectedJsonProvider())
}

/**
 * Shared implementation of the [roundTrip] overloads. Public only because a public inline function may not reference
 * private declarations; the leading underscore marks it as nothing to call directly.
 */
@IgnorableReturnValue
@Suppress("FunctionName")
fun <T> _roundTrip(
  encoder: Json,
  serializer: KSerializer<T>,
  objectToSerialize: T,
  comparisonCheck: ComparisonCheck<T>,
  expectedJson: String?,
): T {
  val json = testSerialization(objectToSerialize, encoder, serializer, expectedJson)
  return testDeserialization(encoder, serializer, json, comparisonCheck, objectToSerialize)
}

fun <T> testSerialization(objectToSerialize: T, encoder: Json, serializer: KSerializer<T>, expectedJson: String?): String {
  val json = encoder.encodeToString(serializer, objectToSerialize)

  //println("JSON length: ${json.toByteArray().size}")
  expectedJson?.let {
    JsonUtils.assertJsonEquals(expectedJson, json)
  }
  return json
}

private fun <T> testDeserialization(encoder: Json, serializer: KSerializer<T>, json: String, comparisonCheck: ComparisonCheck<T>, objectToSerialize: T): T {
  val deserialized = encoder.decodeFromString(serializer, json)
  comparisonCheck(deserialized, objectToSerialize)

  return deserialized
}

/**
 * Round trips the objects as a single JSON array and asserts the decoded list equals the original one. Unlike the
 * single-object [roundTrip] the comparison is fixed to `isEqualTo`, so the elements need a meaningful `equals`.
 */
fun <T> roundTripList(
  vararg objectsToSerialize: T,
  expectedJson: String?,
  serializer: KSerializer<T>,
  inclusionStrategy: JsonInclusionStrategy = JsonInclusionStrategy.EncodeDefaultsIncludeNulls,
) {
  val encoder = Json {
    defaultJsonConfiguration(true, inclusionStrategy)
  }

  val listSerializer = ListSerializer(serializer)

  val objectsToSerializeList: List<T> = objectsToSerialize.toList()
  val json = encoder.encodeToString(listSerializer, objectsToSerializeList)

  //println("JSON length: ${json.toByteArray().size}")

  if (expectedJson != null) {
    JsonUtils.assertJsonEquals(expectedJson, json)
  }

  val deserialized = encoder.decodeFromString(listSerializer, json)
  assertThat(deserialized).isEqualTo(objectsToSerializeList)
}


/**
 * Compares a deserialized instance against the original. Signals failure by throwing — the return value is [Unit],
 * so a check that only returns `false` would pass silently.
 *
 * Override the default (`isEqualTo`) for types whose `equals` cannot see the round trip, e.g. classes without
 * `equals` or fields that are deliberately not serialized.
 */
typealias ComparisonCheck<T> = (deserialized: T, originalObject: T) -> Unit


/**
 * Configures JSON settings for serialization tests.
 */
fun JsonBuilder.defaultJsonConfiguration(
  prettyPrintEnabled: Boolean = true,
  /**
   * Defines how values are included when serializing objects to JSON.
   */
  inclusionStrategy: JsonInclusionStrategy,
) {
  prettyPrint = prettyPrintEnabled
  prettyPrintIndent = "  "
  encodeDefaults = inclusionStrategy.encodeDefaults
  explicitNulls = inclusionStrategy.explicitNulls
}
