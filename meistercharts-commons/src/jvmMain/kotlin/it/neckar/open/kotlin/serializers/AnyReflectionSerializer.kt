package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KProperty

/**
 * A helper serializer that serializes all objects using reflection.
 * Does *not* support **deserialization**.
 */
object AnyReflectionSerializer : KSerializer<Any> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnyReflectionSerializer") {
    element<String>("className")
    element<JsonElement>("properties")
  }

  override fun serialize(encoder: Encoder, value: Any) {
    encoder.beginStructure(descriptor).apply {
      val valueKlass = value::class
      encodeStringElement(descriptor, 0, valueKlass.qualifiedName ?: throw IllegalStateException("Cannot serialize anonymous class"))

      val properties = valueKlass.members.filterIsInstance<KProperty<*>>()

      val propName2value = properties.associate { it.name to it.call(value) }
      encodeSerializableElement(descriptor, 1, JsonElement.serializer(), propName2value.toJsonElement())

      endStructure(descriptor)
    }
  }

  override fun deserialize(decoder: Decoder): Any {
    throw UnsupportedOperationException("Not supported")
  }
}
