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
package it.neckar.open.observable

/**
 * Holds dependent objects to avoid premature garbage collection
 */
class DependentObjects : DependentObjectSupport {
  /**
   * Contains objects that should be stored within the observable object.
   * This can be used to avoid premature garbage collection or add specific stuff
   * to the observable object itself.
   */
  private val dependentObjects: MutableMap<Any, Any> = hashMapOf()

  /**
   * Adds a dependent object that is kept
   */
  override fun addDependentObject(key: Any, dependentObject: Any) {
    set(key, dependentObject)
  }

  /**
   * Adds a dependent object that is kept - using the object itself as the key
   */
  override fun addDependentObject(dependentObject: Any) {
    addDependentObject(dependentObject, dependentObject)
  }

  operator fun set(key: Any, dependentObject: Any) {
    dependentObjects[key] = dependentObject
  }

  /**
   * Returns the dependent object for the given key - if there is one
   */
  override fun getDependentObject(key: Any): Any? {
    return get(key)
  }

  /**
   * Returns the dependent object for the given key if there is one
   */
  operator fun get(key: Any): Any? {
    return dependentObjects[key]
  }

  /**
   * Removes the dependent object for the given key
   */
  override fun removeDependentObject(key: Any): Any? {
    return dependentObjects.remove(key)
  }

  /**
   * Clears all dependent objects
   */
  override fun dispose() {
    dependentObjects.clear()
  }
}
