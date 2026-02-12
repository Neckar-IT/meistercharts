package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.serializer

/**
 * Serializer that can be used to serialize <Any> - *if* the object is serializable itself.
 * Must be used with classes that have been annotated with @[Serializable].
 *
 * Serializes the content as a JsonElement.
 * Does not serialize the class name.
 *
 * Does *NOT* support deserialization!!!
 */
object AnySerializableRawSerializer : KSerializer<Any> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnySerializableRawSerializer")

  override fun serialize(encoder: Encoder, value: Any) {
    // Ensure we're working with JSON
    require(encoder is JsonEncoder) {
      "This serializer only supports JSON format. Provided encoder: ${encoder::class}"
    }

    val serializer = try {
      encoder.json.serializersModule.serializer(value::class.java)
    } catch (e: SerializationException) {
      throw IllegalStateException("Please annotate [${value::class.java.name}] with @Serializable", e)
    }

    // Delegate to encoder.encodeJsonElement to avoid tag stack issues
    val jsonElement = try {
      encoder.json.encodeToJsonElement(serializer as KSerializer<Any>, value)
    } catch (e: Exception) {
      throw SerializationException("Failed to serialize [${value::class.java.name}]", e)
    }

    encoder.encodeJsonElement(jsonElement)
  }

  override fun deserialize(decoder: Decoder): Any {
    throw UnsupportedOperationException("Deserialization is not supported.")
  }

  /**
   * Encodes the provided object to a JsonElement
   *
   * @param includeOptionals When true, includes properties with default values (including computed properties)
   * and explicitly serializes null values. This ensures computed properties are included in the output,
   * which is necessary for OpenAPI examples where computed properties are marked as required.
   *
   * Note: The TypeScript example generator (extract-examples.ts) filters out null values to handle
   * the type mismatch where Orval generates `property?: string` instead of `property: string | null`
   * for optional nullable properties.
   */
  fun encodeToJsonElement(elementToEncode: Any, includeOptionals: Boolean = false): JsonElement {
    val json: Json = if (includeOptionals) {
      Json {
        encodeDefaults = true
        explicitNulls = true
      }
    } else {
      Json {
        encodeDefaults = false
      }
    }

    return json.encodeToJsonElement(AnySerializableRawSerializer, elementToEncode)
  }
}
