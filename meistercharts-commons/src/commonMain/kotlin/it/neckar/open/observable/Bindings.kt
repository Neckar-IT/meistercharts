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

/**
 * Nested  binding for observable object.
 *
 * Can be used like this:
 * ```
 * val name = OuterClass("daName").inner
 *  .select {
 *    it.name
 * }
 *
 * class OuterClass(name: String) {
 *  val inner: ObservableObject<InnerClass> = ObservableObject(InnerClass())
 * }
 *
 * class InnerClass {
 *  val name: ObservableString = ObservableString("initial name")
 * }
 * ```
 *
 * The `name` value is updated whenever the `inner` property is changed *and* the `name` property
 * change of the referenced object
 */
fun <T, N> ReadOnlyObservableObject<T>.select(extractNested: (T) -> ReadOnlyObservableObject<N>): ReadOnlyObservableObject<N> {
  //The currently nested value
  var currentNested: ReadOnlyObservableObject<N> = extractNested(value)

  //Holds the nested value
  val nestedObservableObject = ObservableObject(currentNested.value)

  //Register the value change listener
  val nestedValueListener: (N) -> Unit = { newValue ->
    nestedObservableObject.value = newValue
  }

  var disposable = currentNested.consumeImmediately(nestedValueListener)

  //Update the nested
  consumeImmediately { newValue ->
    //Unregister from the old nested
    disposable.dispose()

    currentNested = extractNested(newValue)
    nestedObservableObject.value = currentNested.value

    disposable = currentNested.consumeImmediately(nestedValueListener)
  }

  return nestedObservableObject
}

/**
 * Reduces a list of observables
 */
fun <T, R> List<ReadOnlyObservableObject<T>>.reduce(function: (List<T>) -> R): ReadOnlyObservableObject<R> {
  return reduceObservables(this, function)
}

/**
 * Merges multiple observables with the same type
 */
fun <T, R> reduceObservables(vararg observables: ReadOnlyObservableObject<T>, function: (List<T>) -> R): ReadOnlyObservableObject<R> {
  return reduceObservables(observables.toList(), function)
}

/**
 * Merges multiple observables with the same type into one single observable
 */
fun <T, R> reduceObservables(observables: List<ReadOnlyObservableObject<T>>, function: (List<T>) -> R): ReadOnlyObservableObject<R> {
  fun extractValues(): List<T> {
    return observables.map { it.value }
  }

  val intermediateObservable = ObservableObject(function(extractValues()))

  observables.fastForEach {
    it.consumeImmediately {
      intermediateObservable.value = function(extractValues())
    }
  }

  return intermediateObservable
}
