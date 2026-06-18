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

import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable

/**
 * Base class for observables (which does not have a value!)
 */
open class DefaultObservable<T> : Observable<T>, Disposable, DependentObjectSupport {
  /**
   * The listeners that are notified about changes
   */
  private val valueChangeListeners: MutableList<ConsumeChangesAction<T>> = mutableListOf()

  /**
   * Dependent objects - to avoid premature GC
   */
  private val dependentObjects: DependentObjects = DependentObjects()

  /**
   * Upstream subscriptions that this observable holds on other observables (via map/bind/select).
   *
   * Disposed when this observable is disposed. Without this, a derived observable cannot be garbage collected
   * while its source is alive, because the source retains the intermediate via its listener list.
   */
  private val upstreamSubscriptions: MutableList<Disposable> = mutableListOf()

  override fun consume(action: ConsumeAction<T>): Disposable {
    return consumeChanges { _, newValue -> action(newValue) }
  }

  override fun consumeChanges(action: ConsumeChangesAction<T>): Disposable {
    valueChangeListeners.add(action)
    return Disposable {
      valueChangeListeners.remove(action)
    }
  }

  /**
   * Adds a dependent object that is kept
   */
  override fun addDependentObject(key: Any, dependentObject: Any) {
    dependentObjects[key] = dependentObject
  }

  override fun addDependentObject(dependentObject: Any) {
    dependentObjects[dependentObject] = dependentObject
  }

  /**
   * Returns the dependent object for the given key - if there is one
   */
  override fun getDependentObject(key: Any): Any? {
    return dependentObjects[key]
  }

  /**
   * Removes the dependent object for the given key
   */
  override fun removeDependentObject(key: Any): Any? {
    return dependentObjects.removeDependentObject(key)
  }

  /**
   * Notifies the listeners about a value change - if the value has changed
   * This method only notifies the listeners when the value has changed
   */
  fun notifyListenersIfChanged(oldValue: T, newValue: T) {
    if (oldValue == newValue) {
      //Nothing has changed, just return
      return
    }

    notifyListeners(oldValue, newValue)
  }

  /**
   * Notifies the listeners - even if [oldValue] and [newValue] are the same
   */
  fun notifyListeners(oldValue: T, newValue: T) {
    // Snapshot before iterating: a listener may dispose itself (or another listener) during the call,
    // which removes from valueChangeListeners and would otherwise corrupt the iteration.
    valueChangeListeners.toList().fastForEach {
      it(oldValue, newValue)
    }
  }

  /**
   * Registers a subscription on another observable that this instance depends on.
   *
   * Must be called when this observable is derived from another via map/bind/select — the returned
   * [Disposable] from `consume`/`consumeImmediately`/`consumeChanges*` must be passed here so it is
   * released in [dispose].
   */
  fun addUpstreamSubscription(subscription: Disposable) {
    upstreamSubscriptions.add(subscription)
  }

  /**
   * Disposes the observable object:
   * Disposes all upstream subscriptions, removes all listeners, and clears dependent objects.
   */
  override fun dispose() {
    upstreamSubscriptions.fastForEach { it.dispose() }
    upstreamSubscriptions.clear()
    valueChangeListeners.clear()
    dependentObjects.dispose()
  }
}
