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

import it.neckar.open.kotlin.lang.getAllSealedSubclasses
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.nonNullOriginal
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.GeneratedSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

/**
 * The name this property is serialized under: its `@SerialName`, else its Kotlin name.
 */
fun KProperty1<*, *>.serialName(): String {
  return findAnnotation<SerialName>()?.value ?: name
}

/**
 * The serializer the serializer of this class writes the element at the end of [path] with — the one the compiler
 * plugin chose, a `@Serializable(with)` on the property, its type alias or its class included.
 *
 * Every step of [path] is resolved in the class the previous step holds, so a property an interface declares is read
 * from the serializer of the document that stores it. A step through a nested class is checked against the descriptor
 * the document writes.
 */
fun KClass<*>.elementSerializer(path: List<KProperty1<*, *>>): KSerializer<*> {
  require(path.isNotEmpty()) { "The path into <$this> names no property" }

  var holder: KClass<*> = this
  path.dropLast(1).forEach { step ->
    val stored: KSerializer<*> = holder.childSerializer(step.serialName())
    holder = step.heldClass()
    val held: SerialDescriptor = holder.classSerializer().descriptor
    require(stored.heldDescriptor() == held) {
      "<$step> is written as <${stored.descriptor.serialName}>, not by the serializer of <$holder>"
    }
  }
  return holder.childSerializer(path.last().serialName())
}

/**
 * The serializer the generated serializer [this] writes the element [name] with.
 */
@OptIn(InternalSerializationApi::class)
fun KSerializer<*>.elementSerializer(name: String): KSerializer<*> {
  require(this is GeneratedSerializer<*>) { "<${descriptor.serialName}> is written by <${this::class}>, which exposes no serializer per element" }
  return childSerializers()[descriptor.requireElementIndex(name)]
}

/**
 * The serializer of the element [name] in a document of this class. The subclasses of a sealed class that store the
 * element write it alike; they may differ in nullability only. A subclass that does not store it, such as one
 * overriding the property with a constant, holds no document the element matches.
 */
@OptIn(ExperimentalSerializationApi::class)
private fun KClass<*>.childSerializer(name: String): KSerializer<*> {
  if (isSealed.not()) {
    return classSerializer().elementSerializer(name)
  }

  val storing = getAllSealedSubclasses()
    .map { subclass -> subclass to subclass.classSerializer() }
    .filter { (_, serializer) -> serializer.descriptor.getElementIndex(name) != CompositeDecoder.UNKNOWN_NAME }
    .map { (subclass, serializer) -> subclass to serializer.elementSerializer(name) }
  require(storing.isNotEmpty()) { "No subclass of <$this> stores <$name>" }

  val descriptors = storing.map { (_, serializer) -> serializer.descriptor.nonNullOriginal }.distinct()
  require(descriptors.size == 1) {
    "The subclasses of <$this> serialize <$name> differently: ${storing.joinToString { (subclass, serializer) -> "${subclass.simpleName}: ${serializer.descriptor.serialName}" }}"
  }
  return storing.first().second
}

/**
 * The serializer of this class. A class with type parameters has none without its type arguments.
 */
@OptIn(InternalSerializationApi::class)
private fun KClass<*>.classSerializer(): KSerializer<*> {
  require(typeParameters.isEmpty()) { "<$this> has type parameters; resolve its elements through the serializer of its collection" }
  return serializer()
}

/**
 * The class one step of a path holds: the element class of a collection, else the class of the property.
 */
private fun KProperty1<*, *>.heldClass(): KClass<*> {
  val held: KType = when {
    returnType.isCollection() -> requireNotNull(returnType.arguments.single().type) { "<$this> holds a star-projected collection" }
    else -> returnType
  }
  val classifier = held.classifier
  require(classifier is KClass<*>) { "<$this> holds the type parameter <$classifier>" }
  return classifier
}

private fun KType.isCollection(): Boolean {
  val classifier = classifier
  return classifier is KClass<*> && classifier.isSubclassOf(Collection::class)
}

/**
 * The descriptor of the value one step holds: the element of a collection, else the value itself, without nullability.
 */
@OptIn(ExperimentalSerializationApi::class)
private fun KSerializer<*>.heldDescriptor(): SerialDescriptor {
  return when (descriptor.kind) {
    StructureKind.LIST -> descriptor.getElementDescriptor(0).nonNullOriginal
    else -> descriptor.nonNullOriginal
  }
}
