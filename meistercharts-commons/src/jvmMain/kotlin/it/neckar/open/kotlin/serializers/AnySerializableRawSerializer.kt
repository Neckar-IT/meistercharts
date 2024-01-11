package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

/**
 * Serializer that can be used to serialize <Any> - *if* the object is serializable itself.
 * Must be used with classes that have been annotated with @[Serializable].
 *
 * Serializes the content as a JsonElement.
 * Does not serialize the class name
 *
 * Does *NOT* support deserialization!!!
 */
object AnySerializableRawSerializer : KSerializer<Any> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnySerializableRawSerializer") {
  }

  override fun serialize(encoder: Encoder, value: Any) {
    val serializer = try {
      serializer(value::class.java)
    } catch (e: SerializationException) {
      throw IllegalStateException("Please annotate [${value::class.java.name}] with @Serializable", e)
    }

    //Delegate to the serializer
    try {
      serializer.serialize(encoder, value)
    } catch (e: Exception) {
      throw SerializationException("Failed to serialize [${value::class.java.name}]", e)
    }
  }

  override fun deserialize(decoder: Decoder): Any {
    throw UnsupportedOperationException("Not supported")
  }

  /**
   * Encodes the provided object to a json string
   */
  fun encodeToJsonElement(elementToEncode: Any): JsonElement {
    return when (elementToEncode) {
      //Work around for bug!
      is String -> Json.encodeToJsonElement(String.serializer(), elementToEncode)
      else -> Json.encodeToJsonElement(AnySerializableRawSerializer, elementToEncode)
    }

  }
}
