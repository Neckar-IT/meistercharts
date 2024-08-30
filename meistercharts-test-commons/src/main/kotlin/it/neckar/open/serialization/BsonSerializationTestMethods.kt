package it.neckar.open.serialization

import assertk.*
import assertk.assertions.*
import it.neckar.open.mongodb.Mongo
import it.neckar.open.test.utils.JsonUtils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.apache.commons.io.output.StringBuilderWriter
import org.bson.BsonBinaryReader
import org.bson.BsonBinaryWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.kotlinx.BsonConfiguration
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import org.bson.io.BasicOutputBuffer
import org.bson.json.JsonReader
import org.bson.json.JsonWriter
import java.nio.ByteBuffer
import kotlin.reflect.KClass


/**
 * Tests serialization round trip
 */
inline fun <reified T : Any> roundTripBson(
  objectToSerialize: T,
  serializer: KSerializer<T>,
  serializersModule: SerializersModule = EmptySerializersModule(),
  noinline comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  expectedBson: String?,
) {
  roundTripBson(objectToSerialize = objectToSerialize, serializer = serializer, serializersModule = serializersModule, comparisonCheck = comparisonCheck) { expectedBson }
}

/**
 * Tests the round trip. If the [expectedBsonProvider] provides null, the resulting JSON will not be checked
 */
inline fun <reified T : Any> roundTripBson(
  objectToSerialize: T,
  serializer: KSerializer<T> = serializer(),
  serializersModule: SerializersModule = EmptySerializersModule(),
  /**
   * The bson configuration - which is used to configure the bson serialization (e.g., include null values)
   */
  bsonConfiguration: BsonConfiguration = Mongo.bsonConfiguration,
  noinline comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  noinline expectedBsonProvider: () -> String?,
): T {

  val codec = KotlinSerializerCodec.create(
    kClass = T::class,
    serializer = serializer,
    serializersModule = serializersModule,
    bsonConfiguration = bsonConfiguration
  )

  return roundTripBson(T::class, objectToSerialize, codec, comparisonCheck, expectedBsonProvider)
}

inline fun <reified T : Any> roundTripBson(
  objectToSerialize: T,
  codec: Codec<T>,
  noinline comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  expectedJson: String?,
): T {
  return roundTripBson(T::class, objectToSerialize, codec, comparisonCheck) { expectedJson }
}

/**
 * Returns the deserialize object
 */
fun <T : Any> roundTripBson(
  type: KClass<T>,
  objectToSerialize: T,
  codec: Codec<T>,
  /**
   * Comparison check that is called. Should throw an exception
   */
  comparisonCheck: ComparisonCheck<T> = { deserialized, originalObject ->
    assertThat(deserialized).isEqualTo(originalObject)
  },
  expectedJsonProvider: () -> String?,
): T {

  val stringBuilderWriter = StringBuilderWriter()
  codec.encode(JsonWriter(stringBuilderWriter), objectToSerialize, EncoderContext.builder().build())
  val json = stringBuilderWriter.builder.toString()

  println("JSON length: ${json.toByteArray().size}")


  val basicOutputBuffer = BasicOutputBuffer()
  val bsonBinaryWriter = BsonBinaryWriter(basicOutputBuffer)
  codec.encode(bsonBinaryWriter, objectToSerialize, EncoderContext.builder().build())

  println("Binary length: ${basicOutputBuffer.size()}")
  println("Binary advantage: ${json.toByteArray().size - basicOutputBuffer.size()}")

  expectedJsonProvider()?.let { expectedJson ->
    JsonUtils.assertJsonEquals(expectedJson, json)
  }

  val deserializedFromJson = codec.decode(JsonReader(json), DecoderContext.builder().build())
  comparisonCheck(deserializedFromJson, objectToSerialize)

  val deserializedFromBson = codec.decode(BsonBinaryReader(ByteBuffer.wrap(basicOutputBuffer.toByteArray())), DecoderContext.builder().build())
  comparisonCheck(deserializedFromBson, objectToSerialize)

  return deserializedFromJson
}

/**
 * Serializes a list of objects
 */
fun <T> roundTripBsonList(vararg objectsToSerialize: T, expectedJson: String?, serializer: KSerializer<T>) {
  val encoder = Json {
    prettyPrint = false
    /**
     * encode default properties of Serializable Classes
     * */
    encodeDefaults = true
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
