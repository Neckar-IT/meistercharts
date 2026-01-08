package it.neckar.open.kotlin.serializers

import it.neckar.open.unit.si.ms
import kotlinx.serialization.Serializable

/**
 * Type alias for [Double] annotated with [@ms][ms] that is serialized as ISO-8601 date-time string.
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class MyData(
 *   val timestamp: IsoMillis
 * )
 * ```
 */
typealias IsoMillis = @Serializable(with = DoubleAsIsoDateTimeSerializer::class) @ms Double
