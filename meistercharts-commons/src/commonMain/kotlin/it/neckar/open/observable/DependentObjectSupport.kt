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

import it.neckar.open.dispose.Disposable

/**
 * Offers support for dependent objects that may be registered.
 * This can be used to avoid premature garbage collection.
 *
 * Dependent objects can be registered at this instance and will be held forever.
 *
 */
interface DependentObjectSupport : Disposable {
  /**
   * Adds a dependent object that is held by this instance to avoid premature garbage collection.
   *
   * The dependent object is registered using the given key and can later be resolved again using [getDependentObject]
   */
  fun addDependentObject(key: Any, dependentObject: Any)

  /**
   * Adds a dependent object that is held by this instance to avoid premature garbage collection.
   * Uses the dependentObject itself as key
   */
  fun addDependentObject(dependentObject: Any)

  /**
   * Returns the dependent object for the given key - if one has been registered using one of the [addDependentObject] methods.
   */
  fun getDependentObject(key: Any): Any?

  /**
   * Removes the dependent object for the given key.
   * This method returns the removed, dependent object if there has one been registered using the given key.
   */
  fun removeDependentObject(key: Any): Any?
}
