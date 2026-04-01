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
package it.neckar.open.kotlin.reflect

import kotlin.reflect.KClass

/**
 * Transforms a (Kotlin) fully qualified class name into a format suitable for `Class.forName()`.
 *
 * This method processes a fully qualified class name (FQCN) and converts it into a format
 * that is compatible with `Class.forName()`, especially handling inner class names correctly.
 * It identifies the part of the FQCN that represents classes (including inner classes),
 * which start with an uppercase letter, and transforms the dots (.) to dollar signs ($)
 * for these class names. This is essential because Java uses dollar signs to distinguish
 * inner classes in its internal naming convention.
 *
 * @param fqName The fully qualified class name to be transformed. It can be a name of
 *               a top-level class or an inner class.
 * @return A string representing the transformed class name, suitable for use with
 *         `Class.forName()`. If the input does not represent an inner class, it returns
 *         the original name.
 */
fun fqNameToClassForName(fqName: String): String {
  val parts = fqName.split('.')

  // Find the index where the name parts start with an uppercase letter.
  val classIndex = parts.indexOfFirst { it.isNotEmpty() && it[0].isUpperCase() }

  // If no part starts with an uppercase letter, return the original name.
  if (classIndex == -1) return fqName

  // Reassemble the name, replacing dots with dollar signs from the found index onwards.
  // A dot is added between the package name and the first class name.
  return parts.subList(0, classIndex).joinToString(".") +
    "." +
    parts.subList(classIndex, parts.size).joinToString("$")
}

/**
 * Kotlin’s public collection types have JVM implementations in `java.util.*`. When we deserialize
 * a descriptor we only see the Kotlin FQCN (`kotlin.collection.ArrayList`, typo included), so we
 * keep a lookup table that forwards those names to the actual runtime classes `Class.forName`
 * understands.
 */
private val kotlinCollectionClassMappings: Map<String, KClass<*>> = mapOf(
  "kotlin.collections.List" to List::class,
  "kotlin.collections.MutableList" to MutableList::class,
  "kotlin.collections.Collection" to Collection::class,
  "kotlin.collections.MutableCollection" to MutableCollection::class,
  "kotlin.collections.Set" to Set::class,
  "kotlin.collections.MutableSet" to MutableSet::class,
  "kotlin.collections.Map" to Map::class,
  "kotlin.collections.MutableMap" to MutableMap::class,
  "kotlin.collections.Iterable" to Iterable::class,
  "kotlin.collections.MutableIterable" to MutableIterable::class,
  "kotlin.collections.Iterator" to Iterator::class,
  "kotlin.collections.MutableIterator" to MutableIterator::class,
  "kotlin.collections.ListIterator" to ListIterator::class,
  "kotlin.collections.MutableListIterator" to MutableListIterator::class,
  "kotlin.collections.ArrayList" to List::class,
  "kotlin.collections.HashSet" to Set::class,
  "kotlin.collections.LinkedHashSet" to Set::class,
  "kotlin.collections.HashMap" to MutableMap::class,
  "kotlin.collections.LinkedHashMap" to MutableMap::class
)

/**
 * Creates a new KClass instance for the given fully qualified class name.
 */
fun classForName(className: String): KClass<*> {
  if (className.contains("<") || className.contains(">")) {
    throw IllegalArgumentException("Generic types are not supported: $className")
  }

  val normalized = className
    .removeSuffix("?")
    .replace("kotlin.collection.", "kotlin.collections.")

  kotlinCollectionClassMappings[normalized]?.let { return it }

  return when (normalized) {
    "kotlin.String" -> String::class
    "kotlin.Int" -> Int::class
    "kotlin.Long" -> Long::class
    "kotlin.Short" -> Short::class
    "kotlin.Byte" -> Byte::class
    "kotlin.Float" -> Float::class
    "kotlin.Double" -> Double::class
    "kotlin.Boolean" -> Boolean::class
    "kotlin.Char" -> Char::class
    "kotlin.Unit" -> Unit::class
    else -> {
      val name = fqNameToClassForName(normalized)
      Class.forName(name).kotlin
    }
  }
}
