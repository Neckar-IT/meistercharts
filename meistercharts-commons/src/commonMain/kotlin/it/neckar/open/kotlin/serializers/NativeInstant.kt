package it.neckar.open.kotlin.serializers

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Type alias for [Instant] that is serialized using the native format for each context:
 * - BSON DateTime when used with MongoDB (better queries, smaller storage)
 * - ISO-8601 string when used with JSON (human readable, standard format)
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class MyData(
 *   val createdAt: NativeInstant
 * )
 * ```
 */
typealias NativeInstant = @Serializable(with = InstantSerializer::class) Instant
