package com.meistercharts.history.impl.io


import com.meistercharts.history.impl.HistoryValues
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * Serializer for history values.
 *
 * Converts the values to relative values and serializes these.
 *
 * ATTENTION: Currently not used!
 */
@Deprecated("Currently not used")
object HistoryValuesAsRelativeSerializer : KSerializer<HistoryValues> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HistoryValues", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: HistoryValues) {
    val relative = value.makeRelative()
    encoder.encodeSerializableValue(RelativeHistoryValues.serializer(), relative)
  }

  override fun deserialize(decoder: Decoder): HistoryValues {
    return decoder.decodeSerializableValue(RelativeHistoryValues.serializer()).makeAbsolute()
  }
}
