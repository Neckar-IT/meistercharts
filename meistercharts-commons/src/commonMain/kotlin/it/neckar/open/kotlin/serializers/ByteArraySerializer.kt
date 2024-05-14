package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer

/**
 * Serializer for ByteArray
 *
 * Usage:`val foobar: @Serializable(with = ByteArrayBase64Serializer::class) ByteArray?`
 */
expect object ByteArraySerializer : KSerializer<ByteArray> {
}
