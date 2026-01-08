package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * Serializer for [kotlin.time.Instant] that stores the value as a native BSON DateTime.
 *
 * This serializer ensures that [kotlin.time.Instant] values are stored as MongoDB Date types
 * rather than ISO-8601 strings, which provides better query capabilities and
 * smaller storage footprint.
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class MyDocument(
 *   @Serializable(with = InstantSerializer::class)
 *   val timestamp: Instant
 * )
 * ```
 */
expect class InstantSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor
  override fun serialize(encoder: Encoder, value: Instant)
  override fun deserialize(decoder: Decoder): Instant
}
