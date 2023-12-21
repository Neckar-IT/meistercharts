package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Converts "any" value to JSON.
 *
 * Inspired by https://github.com/Kotlin/kotlinx.serialization/issues/296
 */
object AnySerializer : KSerializer<Any?> {
  private val delegateSerializer = JsonPrimitive.serializer()

  override val descriptor: SerialDescriptor = delegateSerializer.descriptor

  override fun serialize(encoder: Encoder, value: Any?) {
    encoder.encodeSerializableValue(delegateSerializer, value.toJsonPrimitive())
  }

  override fun deserialize(decoder: Decoder): Any? {
    val jsonPrimitive = decoder.decodeSerializableValue(delegateSerializer)
    return jsonPrimitive.toAnyValue()
  }
}


/**
 * Converts well known values to JsonPrimitives
 */
private fun Any?.toJsonPrimitive(): JsonPrimitive {
  return when (this) {
    null -> JsonNull
    is JsonPrimitive -> this
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    // add custom convert
    else -> throw IllegalArgumentException("Unsupported type: ${this::class}")
  }
}

private fun JsonPrimitive.toAnyValue(): Any? {
  val content = this.content
  if (this.isString) {
    // add custom string convert
    return content
  }
  if (content.equals("null", ignoreCase = true)) {
    return null
  }
  if (content.equals("true", ignoreCase = true)) {
    return true
  }
  if (content.equals("false", ignoreCase = true)) {
    return false
  }
  val intValue = content.toIntOrNull()
  if (intValue != null) {
    return intValue
  }
  val longValue = content.toLongOrNull()
  if (longValue != null) {
    return longValue
  }
  val doubleValue = content.toDoubleOrNull()
  if (doubleValue != null) {
    return doubleValue
  }
  throw IllegalArgumentException("Unsupported content： $content")
}
