/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
