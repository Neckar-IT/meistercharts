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
package it.neckar.open.properties

import kotlin.reflect.KProperty1


/**
 * Contains several properties that form a path.
 */
data class PropertiesPath<T>(
  private val properties: List<KProperty1<*, *>>
) {

  fun properties(): List<KProperty1<*, *>> {
    return properties
  }

  /**
   * Returns a string representation of the path.
   */
  fun asString(): String {
    require(properties.isNotEmpty()) { "Cannot create a string representation of an empty path" }
    return properties.joinToString(separator = ".") { it.name }
  }


  fun <ChildType> add(property: KProperty1<out T, ChildType>): PropertiesPath<ChildType> {
    return this.plus(property)
  }

  /**
   * Creates a new [PropertiesPath] by adding the given child property to the current property.
   */
  operator fun <ChildType> plus(property: KProperty1<out T, ChildType>): PropertiesPath<ChildType> {
    return PropertiesPath(properties + property)
  }

  override fun toString(): String {
    return "PropertiesPath(path=${properties.joinToString(separator = ".") { it.name }})"
  }

  companion object {
    /**
     * Creates a new [PropertiesPath] containing the given property.
     */
    operator fun <T> invoke(property: KProperty1<*, T>): PropertiesPath<T> {
      return PropertiesPath<Any>(listOf(property)) as PropertiesPath<T>
    }

    /**
     * Creates an empty [PropertiesPath].
     * Usually this method should not be used directly.
     */
    operator fun invoke(): PropertiesPath<Any> {
      return PropertiesPath(emptyList())
    }
  }
}

/**
 * Creates a new [PropertiesPath] by adding the given child property to the current property.
 */
operator fun <Parent, Child, ValueType> KProperty1<Parent, Child?>.plus(childProperty: KProperty1<Child, ValueType>): PropertiesPath<ValueType> {
  return PropertiesPath(this).plus(property = childProperty)
}
