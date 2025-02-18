package it.neckar.open.serialization

import assertk.*
import assertk.assertions.*
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

  comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },

  //language=JSON
  json: () -> String,
): T {
  val encoder = Json {
    defaultJsonConfiguration(true)
  }

  return testDeserialization(encoder, serializer, json(), comparisonCheck, expected)
}

fun <T> testDeserialization(
  serializer: KSerializer<T>,
  expected: T,

  comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },

  //language=JSON
  json: String,
): T {
  return testDeserialization(serializer, expected, comparisonCheck) { json }
}

/**
 * Tests serialization round trip
 */
inline fun <reified T> roundTrip(
  objectToSerialize: T,
  serializer: KSerializer<T>,
  serializersModule: SerializersModule = EmptySerializersModule(),
  noinline comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  expectedJson: String?,
) {
  roundTrip(objectToSerialize, serializer, serializersModule, comparisonCheck) { expectedJson }
}

/**
 * Tests the round trip. If the [expectedJsonProvider] provides null, the resulting JSON will not be checked
 */
inline fun <reified T> roundTrip(
  objectToSerialize: T,
  serializer: KSerializer<T> = serializer(),
  serializersModule: SerializersModule = EmptySerializersModule(),
  noinline comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  noinline expectedJsonProvider: () -> String?,
): T {
  val encoder: Json = Json {
    this.serializersModule = serializersModule
    this.defaultJsonConfiguration(true)
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
 * Returns the deserialize object
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
  encoder: Json, serializer: KSerializer<T>,
  objectToSerialize: T,
  comparisonCheck: ComparisonCheck<T>,
  expectedJson: String?,
): T {
  val json = encoder.encodeToString(serializer, objectToSerialize)

  //println("JSON length: ${json.toByteArray().size}")
  expectedJson?.let {
    JsonUtils.assertJsonEquals(expectedJson, json)
  }

  return testDeserialization(encoder, serializer, json, comparisonCheck, objectToSerialize)
}

private fun <T> testDeserialization(encoder: Json, serializer: KSerializer<T>, json: String, comparisonCheck: ComparisonCheck<T>, objectToSerialize: T): T {
  val deserialized = encoder.decodeFromString(serializer, json)
  comparisonCheck(deserialized, objectToSerialize)

  return deserialized
}

/**
 * Serializes a list of objects
 */
fun <T> roundTripList(vararg objectsToSerialize: T, expectedJson: String?, serializer: KSerializer<T>) {
  val encoder = Json {
    defaultJsonConfiguration(true)
  }

  val listSerializer = ListSerializer(serializer)

  val objectsToSerializeList: List<T> = objectsToSerialize.toList()
  val json = encoder.encodeToString(listSerializer, objectsToSerializeList)

  //println("JSON length: ${json.toByteArray().size}")

  if (expectedJson != null) {
    JsonUtils.assertJsonEquals(expectedJson, json)
  }

  val deserialized = Json.decodeFromString(listSerializer, json)
  assertThat(deserialized).isEqualTo(objectsToSerializeList)
}


/**
 * Compares
 */
typealias ComparisonCheck<T> = (deserialized: T, originalObject: T) -> Unit


/**
 * Copied from `JsonElementExtKt`
 */
fun JsonBuilder.defaultJsonConfiguration(prettyPrintEnabled: Boolean = true) {
  prettyPrint = prettyPrintEnabled
  prettyPrintIndent = "  "
  /**
   * encode default properties of Serializable Classes
   * */
  encodeDefaults = true
}
