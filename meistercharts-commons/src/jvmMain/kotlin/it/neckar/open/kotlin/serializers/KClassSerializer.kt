package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

/**
 * Serializes a [KClass] by its fully qualified name
 */
object KClassSerializer : KSerializer<KClass<*>> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("KType", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: KClass<*>) {
    encoder.encodeString(value.qualifiedName ?: throw IllegalStateException("Cannot serialize anonymous class"))
  }

  override fun deserialize(decoder: Decoder): KClass<*> {
    decoder.decodeString().let { className ->
      return getKotlinClassByName(className)
    }
  }

  fun getKotlinClassByName(name: String): KClass<*> {
    return when (name) {
      "kotlin.Int" -> Int::class
      "kotlin.Long" -> Long::class
      "kotlin.Double" -> Double::class
      "kotlin.Float" -> Float::class
      "kotlin.Boolean" -> Boolean::class
      "kotlin.Char" -> Char::class
      "kotlin.Byte" -> Byte::class
      "kotlin.Short" -> Short::class
      "kotlin.String" -> String::class
      else -> Class.forName(name).kotlin
    }
  }
}
