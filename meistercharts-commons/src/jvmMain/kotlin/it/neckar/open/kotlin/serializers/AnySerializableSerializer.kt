package it.neckar.open.kotlin.serializers

import it.neckar.open.kotlin.lang.requireNotNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

/**
 * Serializer that can be used to serialize <Any> - *if* the object is serializable itself.
 *
 * Must be used with ]
 */
object AnySerializableSerializer : KSerializer<Any> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnyReflectionSerializer") {
    element<String>("className")
    element<JsonElement>("content")
  }

  override fun serialize(encoder: Encoder, value: Any) {
    encoder.encodeStructure(descriptor) {
      val qualifiedName = value::class.java.name
      encodeStringElement(descriptor, 0, qualifiedName ?: throw IllegalStateException("Cannot serialize anonymous class"))

      val serializer = try {
        serializer(value::class.java)
      } catch (e: SerializationException) {
        throw IllegalStateException("Please annotate [$qualifiedName] with @Serializable", e)
      }

      encodeSerializableElement(descriptor, 1, serializer, value)
    }
  }

  override fun deserialize(decoder: Decoder): Any {
    return decoder.decodeStructure(descriptor) {
      var className: String? = null
      var data: Any? = null

      while (true) {
        when (val index = decodeElementIndex(descriptor)) {
          0 -> className = decodeStringElement(descriptor, 0)
          1 -> {
            requireNotNull(className) { "className must be set before data" }

            val clazz = Class.forName(className)
            val serializer = serializer(clazz)
            data = decodeSerializableElement(descriptor, 1, serializer)

          }

          CompositeDecoder.DECODE_DONE -> break
          else -> error("Unexpected index: $index")
        }
      }

      data.requireNotNull()
    }
  }
}
