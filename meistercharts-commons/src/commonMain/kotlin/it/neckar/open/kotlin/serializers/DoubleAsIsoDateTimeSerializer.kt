package it.neckar.open.kotlin.serializers

import it.neckar.open.unit.si.ms
import kotlinx.serialization.KSerializer

/**
 * Formats a double value as iso format.
 *
 * ATTENTION: Does *not* support nanoseconds!
 *
 * Use like this:
 * `@Serializable(with = DoubleAsIsoDateTimeSerializer::class)`
 */
expect object DoubleAsIsoDateTimeSerializer : KSerializer<@ms Double>
