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
import it.neckar.open.collections.fastMap
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.unit.si.N

/**
 * Represents an observable list.
 * This list must not be modified!
 *
 * Update the complete list at once.
 */
class ObservableList<T>(initValue: List<T>) : ReadOnlyObservableList<T>, ObservableObject<List<T>>(initValue) {

  /**
   * Maps the elements of the list to another value.
   * Returns an observable list of the new type that is automatically updated.
   *
   * The returned list holds the upstream subscription on this observable and releases it on [dispose].
   */
  fun <R> mapElements(mapFunction: (T) -> R): ReadOnlyObservableList<R> {
    val mappedInitially = this.value.map { mapFunction(it) }

    return ObservableList(mappedInitially).also {
      it.addUpstreamSubscription(consume { newValue ->
        val mapped = newValue.map { mapFunction(it) }
        it.value = mapped
      })
    }
  }

  companion object {
    fun <T> empty(): ObservableList<T> = ObservableList(emptyList())
  }
}

interface ReadOnlyObservableList<T> : ReadOnlyObservableObject<List<T>>

/**
 * Returns a list of observables that are extracted from the given list.
 */
fun <T : Observable<*>, N : Any?> ReadOnlyObservableList<T>.selectList(extractNested: (T) -> ReadOnlyObservableObject<N>): ReadOnlyObservableList<N> {
  //Contains the results. Will be updated whenever
  //* the list itself changes
  //* one of the inner values changes
  val resultList: ObservableList<N> = ObservableList(emptyList())


  //Helper function to recalculate the results
  fun recalculateResults() {
    resultList.value = value.fastMap { observableObject: T ->
      extractNested(observableObject).value
    }
  }

  //Update the listeners, whenever the content of this changes
  val disposableSupport = DisposeSupport(mode = DisposeSupport.Mode.MultiDispose)

  //Is registered as listener for every inner property
  val innerPropertyConsumer: (newValue: Any?) -> Unit = { _ ->
    //Update the results, whenever one of the inner values changes
    recalculateResults()
  }

  //Register the listeners - tracked on resultList so dispose releases both the outer subscription and all inner listeners
  resultList.addUpstreamSubscription(this.consumeImmediately { elements ->
    //Dispose the old listeners
    disposableSupport.dispose()

    //Register at each element
    elements.fastForEach { innerProperty ->
      disposableSupport.onDispose(
        innerProperty.consume(action = innerPropertyConsumer)
      )
    }

    //Recalculate the results
    recalculateResults()
  })
  //Forward dispose to the inner-listener container
  resultList.addUpstreamSubscription(disposableSupport)

  return resultList
}


/**
 * Returns an observable, that is notified about all changes of all observables within this [ReadOnlyObservableList].
 *
 * The observable notifies its observers about:
 * * changes to the list itself
 * * changes to the inner observables
 *
 */
fun <T> ReadOnlyObservableList<T>.selectListObservable(
  /**
   * Extracts the observable from the given element.
   * This method will be called for each element in the [ReadOnlyObservableList] - whenever the content of the [ReadOnlyObservableList] changes.
   */
  extractNested: (T) -> Observable<Any?>,
): Observable<Any?> {
  val resultObservable = DefaultObservable<Any?>()

  //Update the listeners, whenever the content of this changes
  val disposableSupport = DisposeSupport(mode = DisposeSupport.Mode.MultiDispose)

  //Is registered as listener for every inner property
  val innerPropertyConsumer: (oldValue: Any?, newValue: Any?) -> Unit = { oldValue, newValue ->
    resultObservable.notifyListeners(oldValue, newValue)
  }

  //Register the listeners at the observables in the list - tracked on resultObservable so dispose releases
  //both the outer subscription and all inner listeners
  resultObservable.addUpstreamSubscription(consumeImmediately { elements ->
    //Dispose the old listeners
    disposableSupport.dispose()

    //Register at each element
    elements.fastForEach { innerProperty ->
      val innerObservable = extractNested(innerProperty)
      innerObservable.consumeChanges(action = innerPropertyConsumer).also {
        disposableSupport.onDispose(it)
      }
    }

    //Notify the listeners about the updated values
    resultObservable.notifyListeners(null, elements)
  })
  //Forward dispose to the inner-listener container
  resultObservable.addUpstreamSubscription(disposableSupport)

  return resultObservable
}
