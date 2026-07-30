/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.kotlin.serializers.meta

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlin.reflect.KClass

/**
 * Represents a serializer model of a [kotlinx.serialization.Serializable] class.
 */
@Deprecated("Required?????")
sealed interface SerializerModel<T : Any> {
  val serialName: String
  val type: KClass<T>

  companion object {
    inline fun <reified T : Any> create(serializer: KSerializer<T>, type: KClass<T> = T::class): SerializerModel<T> {
      val descriptor = serializer.descriptor
      val model = when (descriptor.kind) {
        PrimitiveKind.BOOLEAN -> PrimitiveSerializerModel(descriptor.serialName, Boolean::class)
        PrimitiveKind.BYTE -> PrimitiveSerializerModel(descriptor.serialName, Byte::class)
        PrimitiveKind.CHAR -> PrimitiveSerializerModel(descriptor.serialName, Char::class)
        PrimitiveKind.DOUBLE -> PrimitiveSerializerModel(descriptor.serialName, Double::class)
        PrimitiveKind.FLOAT -> PrimitiveSerializerModel(descriptor.serialName, Float::class)
        PrimitiveKind.INT -> PrimitiveSerializerModel(descriptor.serialName, Int::class)
        PrimitiveKind.LONG -> PrimitiveSerializerModel(descriptor.serialName, Long::class)
        PrimitiveKind.SHORT -> PrimitiveSerializerModel(descriptor.serialName, Short::class)
        PrimitiveKind.STRING -> PrimitiveSerializerModel(descriptor.serialName, String::class)

        StructureKind.CLASS -> {
          StructuredSerializerModel.create(descriptor, type)
        }


        PolymorphicKind.OPEN -> TODO()
        PolymorphicKind.SEALED -> TODO()

        SerialKind.CONTEXTUAL -> TODO()
        SerialKind.ENUM -> TODO()

        StructureKind.LIST -> TODO()
        StructureKind.MAP -> TODO()
        StructureKind.OBJECT -> TODO()
        else -> {
          TODO("Unknown kind ${descriptor.kind}")
        }
      }

      return model as SerializerModel<T>
    }
  }
}

/**
 * Represents a structured serializer model
 */
data class StructuredSerializerModel<T : Any>(
  override val serialName: String,
  override val type: KClass<T>,
  /**
   * The elements of the structured serializer
   */
  val elements: List<SerializerModel<*>>,
) : SerializerModel<T> {
  companion object {
    fun <T : Any> create(descriptor: SerialDescriptor, type: KClass<T>): StructuredSerializerModel<T> {
      val elements: List<SerializerModel<*>> = descriptor.elementDescriptors.map {
        //SerializerModel.create(it.serialName, it.type)
        TODO()
      }

      return StructuredSerializerModel(descriptor.serialName, type, elements)
    }
  }
}

/**
 * Represents a primitive type
 */
data class PrimitiveSerializerModel<T : Any>(
  override val serialName: String,
  override val type: KClass<T>,
) : SerializerModel<T> {

}
