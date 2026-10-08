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
package it.neckar.open.kotlin.serializers

import it.neckar.open.kotlin.lang.requireNotNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass

/**
 * Returns true if this serial kind is a primitive
 */
@OptIn(ExperimentalSerializationApi::class)
val SerialKind.isPrimitive: Boolean
  get() {
    return when (this) {
      PrimitiveKind.BOOLEAN -> true
      PrimitiveKind.BYTE -> true
      PrimitiveKind.CHAR -> true
      PrimitiveKind.DOUBLE -> true
      PrimitiveKind.FLOAT -> true
      PrimitiveKind.INT -> true
      PrimitiveKind.LONG -> true
      PrimitiveKind.SHORT -> true
      PrimitiveKind.STRING -> true
      else -> false
    }
  }

@OptIn(ExperimentalSerializationApi::class)
val SerialKind.toPrimitiveType: KClass<*>?
  get() {
    return when (this) {
      PrimitiveKind.BOOLEAN -> Boolean::class
      PrimitiveKind.BYTE -> Byte::class
      PrimitiveKind.CHAR -> Char::class
      PrimitiveKind.DOUBLE -> Double::class
      PrimitiveKind.FLOAT -> Float::class
      PrimitiveKind.INT -> Int::class
      PrimitiveKind.LONG -> Long::class
      PrimitiveKind.SHORT -> Short::class
      PrimitiveKind.STRING -> String::class
      else -> null
    }
  }


/**
 * Returns the required element names for this descriptor
 * Skips elements that are optional (have a default value)
 */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.requiredElementNames(): List<String> {
  return elementNames.filterIndexed { index, _ ->
    isElementOptional(index).not()
  }
}

/**
 * Returns all element names that are non-nullable
 */
fun SerialDescriptor.nonNullableElementNames(): List<String> {
  return elementNames.filterIndexed { index, _ ->
    isElementNullable(index).not()
  }
}

/**
 * Returns all element names that are non-nullable and required (no default value)
 */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.nonNullableAndRequiredElementNames(): List<String> {
  return elementNames.filterIndexed { index, _ ->
    isElementNullable(index).not() && isElementOptional(index).not()
  }
}

/**
 * Returns true if the element with the given index is nullable
 */
fun SerialDescriptor.isElementNullable(index: Int): Boolean {
  val elementDescriptor = getElementDescriptor(index)
  return elementDescriptor.isNullable
}

/**
 * Returns the serial descriptor for the element with the given name
 */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.getElementDescriptorByName(name: String): SerialDescriptor {
  return getElementDescriptor(requireElementIndex(name))
}

/**
 * The index of the element [name]; throws for a name this descriptor does not carry.
 */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.requireElementIndex(name: String): Int {
  val index = getElementIndex(name)
  require(index != CompositeDecoder.UNKNOWN_NAME) { "<$serialName> has no element <$name>; elements: ${elementNames.joinToString()}" }
  return index
}

/**
 * Throws an exception if this type should not be used for serialization
 */
expect fun <S : Any> KClass<S>.verifyPlausibleForSerialization(): Unit


/**
 * Returns true if the descriptor is a primitive serializer:
 * A serializer which does not have any elements.
 */
fun SerialDescriptor.isPrimitive(): Boolean {
  return this.kind.isPrimitive
}

/**
 * Returns the primitive type of this descriptor if it is a primitive serializer.
 */
fun SerialDescriptor.toPrimitiveType(): KClass<*>? {
  return this.kind.toPrimitiveType
}

/**
 * What the class's own serializer writes. A sealed hierarchy is [SerializedForm.Sealed], whatever its subtypes write.
 */
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
fun KClass<*>.serializedForm(): SerializedForm {
  val serializer = serializerOrNull() ?: return SerializedForm.NoSerializer

  return when (serializer.descriptor.kind) {
    is PrimitiveKind -> SerializedForm.Primitive
    SerialKind.ENUM -> SerializedForm.Enum
    StructureKind.CLASS -> SerializedForm.Class
    StructureKind.OBJECT -> SerializedForm.Object
    StructureKind.LIST -> SerializedForm.List
    StructureKind.MAP -> SerializedForm.Map
    PolymorphicKind.SEALED -> SerializedForm.Sealed
    PolymorphicKind.OPEN -> SerializedForm.Open
    SerialKind.CONTEXTUAL -> SerializedForm.Contextual
  }
}

/**
 * The descriptor of the serializer of [E]; throws if its kind is not `SerialKind.ENUM`, as for a primitive custom serializer.
 * The serial-name lookups rely on its one element per constant, in the order of the constants.
 */
inline fun <reified E : Enum<E>> enumDescriptor(): SerialDescriptor {
  val descriptor: SerialDescriptor = serializer<E>().descriptor
  require(descriptor.kind == SerialKind.ENUM) { "<${descriptor.serialName}> is serialized as ${descriptor.kind}, not as an enum" }
  return descriptor
}

/**
 * The names the serializer of [E] writes, in the order of the constants; throws like [enumDescriptor].
 * Without `@Serializable` on [E], the JVM fallback serializer ignores `@SerialName` and writes the constant names.
 */
inline fun <reified E : Enum<E>> serialNames(): List<String> {
  return enumDescriptor<E>().elementNames.toList()
}

/**
 * The constant of [E] the serializer writes as [serialName]; throws if no constant carries it, or like [enumDescriptor].
 */
inline fun <reified E : Enum<E>> getEnumBySerialName(serialName: String): E {
  return findEnumBySerialName<E>(serialName).requireNotNull {
    "<${enumDescriptor<E>().serialName}> has no entry with serial name <$serialName>; serial names: ${serialNames<E>().joinToString()}"
  }
}

/**
 * The constant of [E] the serializer writes as [serialName], or null if no constant carries it; throws like [enumDescriptor].
 */
inline fun <reified E : Enum<E>> findEnumBySerialName(serialName: String): E? {
  return enumDescriptor<E>().findElementIndex(serialName)?.let { enumValues<E>()[it] }
}

/**
 * The index of the element [name], or null if this descriptor has no such element.
 */
fun SerialDescriptor.findElementIndex(name: String): Int? {
  return getElementIndex(name).takeUnless { it == CompositeDecoder.UNKNOWN_NAME }
}

/**
 * The descriptor of the element [name], or null if this descriptor has no such element.
 */
fun SerialDescriptor.findElementDescriptorByName(name: String): SerialDescriptor? {
  return findElementIndex(name)?.let { getElementDescriptor(it) }
}
