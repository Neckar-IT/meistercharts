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
 * Tests deserialization
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

inline fun <reified T> testSerialization(
  objectToSerialize: T,
  serializer: KSerializer<T> = serializer(),
  serializersModule: SerializersModule = EmptySerializersModule(),
  inclusionStrategy: JsonInclusionStrategy = JsonInclusionStrategy.EncodeDefaultsIncludeNulls,
  //language=JSON
  json: () -> String,
) {
  val encoder = Json {
    this.serializersModule = serializersModule
    this.defaultJsonConfiguration(true, inclusionStrategy)
  }

  testSerialization(objectToSerialize = objectToSerialize, encoder = encoder, serializer = serializer, json())
}

/**
 * Tests serialization round trip
 */
inline fun <reified T> roundTrip(
  objectToSerialize: T,
  serializer: KSerializer<T>,
  serializersModule: SerializersModule = EmptySerializersModule(),
  inclusionStrategy: JsonInclusionStrategy = JsonInclusionStrategy.EncodeDefaultsIncludeNulls,
  noinline comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  expectedJson: String?,
) {
  roundTrip(objectToSerialize, serializer, serializersModule, inclusionStrategy, comparisonCheck) { expectedJson }
}

/**
 * Tests the round trip. If the [expectedJsonProvider] provides null, the resulting JSON will not be checked
 */
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
 * Returns the deserialized object
 */
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
 * Serializes a list of objects
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
 * Compares
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
