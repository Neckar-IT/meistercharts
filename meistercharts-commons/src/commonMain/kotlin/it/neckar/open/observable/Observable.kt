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

import it.neckar.open.annotations.JavaFriendly
import it.neckar.open.dispose.Disposable

/**
 * Consumes new values (but does not receive the old one)
 */
typealias ConsumeAction<T> = (newValue: T) -> Unit

/**
 * Consumes changes - the old and new value is provided
 */
typealias ConsumeChangesAction<T> = (oldValue: T, newValue: T) -> Unit


/**
 * Represents an observable object.
 *
 * This interface only provides the ability to observe changes.
 * It does not provide the ability to access the current value.
 *
 * If the current value is required, use [ReadOnlyObservableObject] instead.
 */
interface Observable<out T> {
  /**
   * Registers an action that will be called when the value is changed.
   * @return a dispose action to unregister the given action
   */
  fun consume(action: ConsumeAction<T>): Disposable

  /**
   * Registers an action that will be called when the value has changed. The given action also gets the old value (if available)
   * @return a dispose action to unregister the given action
   */
  fun consumeChanges(action: ConsumeChangesAction<T>): Disposable


  /**
   * Listener that is notified about value changes
   */
  @JavaFriendly
  fun interface ChangeListener<in T> {
    fun valueChanged(oldValue: T, newValue: T)
  }

  /**
   * Registers a change listener that is notified about value changes
   */
  @JavaFriendly
  fun addChangeListener(listener: ChangeListener<T>): Disposable {
    return consumeChanges(listener::valueChanged)
  }
}
