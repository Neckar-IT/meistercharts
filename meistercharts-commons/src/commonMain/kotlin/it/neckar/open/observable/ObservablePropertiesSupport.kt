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
import it.neckar.open.dispose.DisposeSupport

/**
 * Supports observing changes of multiple properties within a class.
 */
open class ObservablePropertiesSupport(
  properties: List<Observable<*>>,
) : ObservableProperties, Disposable {

  private val disposeSupport = DisposeSupport()

  private val valueChangeListeners: MutableList<ConsumeChangesAction<Any?>> = mutableListOf()

  init {
    properties.fastForEach { property ->
      property.consumeChanges { old, new ->
        notifyChange(old, new)
      }.also {
        disposeSupport.onDispose(it)
      }
    }
  }

  override fun dispose() {
    disposeSupport.dispose()
  }

  override fun consumeAllPropertiesChanges(action: ConsumeChangesAction<Any?>): Disposable {
    valueChangeListeners.add(action)
    return Disposable {
      valueChangeListeners.remove(action)
    }
  }

  /**
   * Notifies all listeners about a change.
   */
  fun notifyChange(oldValue: Any?, newValue: Any?) {
    // Snapshot before iterating: a listener may dispose itself (or another listener)
    // during the call, which removes from valueChangeListeners and would otherwise
    // corrupt the iteration. Same fix as DefaultObservable.notifyListeners.
    valueChangeListeners.toList().fastForEach { listener ->
      listener(oldValue, newValue)
    }
  }

  companion object {
    /**
     * Creates a new [ObservablePropertiesSupport] instance with the given properties.
     */
    operator fun invoke(vararg properties: Observable<*>): ObservablePropertiesSupport {
      return ObservablePropertiesSupport(properties.toList())
    }

    /**
     * Creates a new [ObservablePropertiesSupport] instance with the given properties.
     */
    operator fun invoke(vararg properties: ObservableProperties): ObservablePropertiesSupport {
      return ObservablePropertiesSupport(
        properties.toList()
      )
    }

    /**
     * Builds a new [ObservablePropertiesSupport] instance.
     */
    fun create(action: Builder.() -> Unit): ObservablePropertiesSupport {
      val builder = Builder()
      builder.action()
      return builder.build()
    }
  }

  /**
   * Builder for [ObservablePropertiesSupport].
   */
  class Builder() {
    /**
     * The list of observables that should be observed.
     */
    val observables: MutableList<Observable<*>> = mutableListOf()

    fun build(): ObservablePropertiesSupport {
      return ObservablePropertiesSupport(observables)
    }

    @Deprecated("Use observableList.selectListObservable or addObservableListNoSelected instead", level = DeprecationLevel.ERROR)
    fun addObservable(observableList: ObservableList<*>) {
      addObservable(observableList as Observable<*>)
    }

    /**
     * Adds an observable list to the support.
     * Automatically registers the listeners for all elements in the list.
     */
    fun <T> addObservableListSelected(observableList: ObservableList<T>, extractNestedObservable: (T) -> Observable<Any?>) {
      this.observables.add(observableList.selectListObservable(extractNestedObservable))
    }

    fun <T> addObservableListNoSelected(observableList: ObservableList<T>) {
      this.observables.add(observableList)
    }

    /**
     * Adds a single observable to the support
     */
    fun addObservable(observable: Observable<*>) {
      this.observables.add(observable)
    }
  }
}
