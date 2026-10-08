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

import it.neckar.open.kotlin.lang.asKClass
import it.neckar.open.kotlin.lang.getAllSealedSubclasses
import it.neckar.open.kotlin.lang.isSealed
import it.neckar.open.kotlin.lang.requireNotNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.findAnnotations

actual fun <S : Any> KClass<S>.verifyPlausibleForSerialization() {
  if (isSealed) {
    //all sealed interfaces/classes are plausible targets
    return
  }

  val sealedSuperType: KType? = allSupertypes.firstOrNull { it.isSealed() }

  if (sealedSuperType != null) {
    //We have a sealed interface as supertype, use the sealed interface for serialization
    throw IllegalArgumentException("Use the sealed interface [${sealedSuperType.asKClass().simpleName}] as type for serialization instead of [${this.simpleName}].")
  }
}

/**
 * The serial name from the [SerialName] annotation, or null if the class carries none.
 */
fun KClass<*>.findSerialName(): String? {
  return findAnnotations(SerialName::class).firstOrNull()?.value
}

/**
 * The serial name from the [SerialName] annotation. Throws if the class carries none.
 */
fun KClass<*>.getSerialName(): String {
  return findSerialName().requireNotNull { "<$qualifiedName> carries no @SerialName" }
}

/**
 * Returns the serial name of this enum constant from its [SerialName] annotation, or null.
 * kotlinx.serialization serializes an enum constant as exactly this value (falling back to [Enum.name]).
 */
fun Enum<*>.findSerialName(): String? {
  // Enum constants with a body compile to anonymous subclasses - the field lives on the enum class itself.
  val enumJavaClass = javaClass.let { if (it.isEnum) it else it.superclass }
  return enumJavaClass.getField(name).getAnnotation(SerialName::class.java)?.value
}

/**
 * The [SerialName] of this enum constant, else [Enum.name]: the name the serializer of a `@Serializable` enum writes.
 */
fun Enum<*>.serialName(): String {
  return findSerialName() ?: name
}

/**
 * The [SerialName] of every subclass [getAllSealedSubclasses] returns: the values the class discriminator writes.
 * Throws if this class is not sealed or a subclass carries no [SerialName].
 */
fun <T : Any> KClass<T>.getDiscriminatorValues(): List<String> {
  return getAllSealedSubclasses().map { it.getSerialName() }
}

/**
 * Returns true if the class is annotated as serializable
 */
fun KClass<*>.isAnnotatedAsSerializable(): Boolean {
  return findAnnotations(kotlinx.serialization.Serializable::class).isNotEmpty()
}

/**
 * Tries to find the serial descriptor for the given type (includes generic types)
 */
fun KType.serialDescriptor(): SerialDescriptor {
  return serializer(this).descriptor
}

private val emptySerializersModule = EmptySerializersModule()

/**
 * Tries to find the serializer for the given type (includes generic types)
 */
fun KType.serializer(): KSerializer<*> {
  try {
    return emptySerializersModule.serializer(this)
  } catch (e: SerializationException) {
    throw IllegalStateException("Cannot find serializer for type ${this}", e)
  }
}

/**
 * The serializer for the given type (includes generic types), or null if kotlinx.serialization has none for it.
 */
fun KType.serializerOrNull(): KSerializer<*>? {
  return emptySerializersModule.serializerOrNull(this)
}
