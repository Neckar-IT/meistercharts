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
import kotlin.reflect.KProperty


/**
 * Observable object - read-only view
 *
 */
interface ReadOnlyObservableObject<out T> : Observable<T>, DependentObjectSupport {
  /**
   * The current value of the observable object
   */
  val value: T


  /**
   * Returns the value - used for delegation to a val:
   * ```
   * val nameProperty = ObservableObject("asdf")
   * val name by nameProperty
   * ```
   *
   * For a binding to a `var` look at [it.neckar.open.observable.ObservableObject.setValue(java.lang.Object, kotlin.reflect.KProperty<?>, T)]
   */
  operator fun getValue(thisRef: Any, property: KProperty<*>): T {
    return value
  }

  /**
   * Registers an action that is called when the value is changed
   */
  fun consume(immediately: Boolean = false, action: ConsumeAction<T>): Disposable {
    return consumeChanges(immediately = immediately) { _, newValue -> action(newValue) }
  }

  /**
   * Registers an action that is called immediately (with the current value) and when the value is changed
   */
  fun consumeImmediately(action: ConsumeAction<T>): Disposable {
    return consume(immediately = true, action = action)
  }

  fun consumeChanges(immediately: Boolean = false, action: ConsumeChangesAction<T>): Disposable {
    return consumeChanges(action = action).also {
      if (immediately) {
        action(value, value)
      }
    }
  }

  /**
   * Registers an action that is called immediately (with the current value) and when the value is changed
   */
  fun consumeChangesImmediately(action: ConsumeChangesAction<T>): Disposable {
    return consumeChanges(immediately = true, action = action)
  }

  /**
   * Maps the value of the current observable object to another value.
   *
   * The returned observable holds the upstream subscription on this observable and releases it on [dispose].
   */
  fun <R> map(mapFunction: (T) -> R): DisposableReadOnlyObservableObject<R> {
    //This method cannot be pushed down, since the value is required
    val intermediateObservable = ObservableObject(initValue = mapFunction(value))

    intermediateObservable.addUpstreamSubscription(consume { newValue ->
      intermediateObservable.value = mapFunction(newValue)
    })
    return intermediateObservable
  }

  /**
   * Maps the value of the current observable object to a boolean.
   * Returns a new observable boolean that is automatically updated.
   *
   * The returned observable holds the upstream subscription on this observable and releases it on [dispose].
   */
  fun mapBoolean(mapFunction: (T) -> Boolean): ReadOnlyObservableBoolean {
    val intermediateObservable = ObservableBoolean(mapFunction(value))

    intermediateObservable.addUpstreamSubscription(consume { newValue ->
      intermediateObservable.value = mapFunction(newValue)
    })
    return intermediateObservable
  }

  /**
   * Use this observable and another observable and map these two values into a new observable.
   *
   * The returned observable holds the upstream subscriptions on both source observables and releases them on [dispose].
   */
  fun <U, R> map(otherObservable: ReadOnlyObservableObject<U>, function: (T, U) -> R): DisposableReadOnlyObservableObject<R> {
    val intermediateObservable = ObservableObject(function(value, otherObservable.value))

    intermediateObservable.addUpstreamSubscription(consume { newValue ->
      intermediateObservable.value = function(newValue, otherObservable.value)
    })
    intermediateObservable.addUpstreamSubscription(otherObservable.consume { newValue ->
      intermediateObservable.value = function(value, newValue)
    })

    return intermediateObservable
  }
}

/**
 * Use this observable and two other observables to create a new value
 */
fun <T, U, V, R> ReadOnlyObservableObject<T>.map(
  otherObservable1: ReadOnlyObservableObject<U>,
  otherObservable2: ReadOnlyObservableObject<V>, function: (T, U, V) -> R,
): DisposableReadOnlyObservableObject<R> {
  val intermediateObservable = ObservableObject(function(value, otherObservable1.value, otherObservable2.value))

  intermediateObservable.addUpstreamSubscription(consume { newValue ->
    intermediateObservable.value = function(newValue, otherObservable1.value, otherObservable2.value)
  })
  intermediateObservable.addUpstreamSubscription(otherObservable1.consume { newValue ->
    intermediateObservable.value = function(value, newValue, otherObservable2.value)
  })
  intermediateObservable.addUpstreamSubscription(otherObservable2.consume { newValue ->
    intermediateObservable.value = function(value, otherObservable1.value, newValue)
  })

  return intermediateObservable
}

/**
 * Reduces this observable with the other observables into a single observable
 */
fun <T, R> ReadOnlyObservableObject<T>.reduce(vararg otherObservables: ReadOnlyObservableObject<T>, function: (List<T>) -> R): DisposableReadOnlyObservableObject<R> {
  val otherObservables1 = listOf(this, *otherObservables)
  return reduceObservables(otherObservables1, function)
}


/**
 * Creates a new observable boolean that holds "or"
 */
infix fun ReadOnlyObservableObject<Boolean>.or(other: ReadOnlyObservableObject<Boolean>): DisposableReadOnlyObservableObject<Boolean> {
  return map(other) { myValue, otherValue ->
    return@map myValue || otherValue
  }
}

/**
 * Creates a new observable boolean that holds "and"
 */
infix fun ReadOnlyObservableObject<Boolean>.and(other: ObservableObject<Boolean>): DisposableReadOnlyObservableObject<Boolean> {
  return map(other) { myValue, otherValue ->
    return@map myValue && otherValue
  }
}

/**
 * Connects multiple observables using or
 */
fun ReadOnlyObservableObject<Boolean>.or(vararg other: ReadOnlyObservableObject<Boolean>): DisposableReadOnlyObservableObject<Boolean> {
  return reduce(*other) {
    it.reduce { bool1, bool2 ->
      bool1 || bool2
    }
  }
}

/**
 * Returns an observable that connects all values of this list using `and`.
 * Does not work with an empty list
 */
fun ReadOnlyObservableObject<Boolean>.and(vararg other: ObservableObject<Boolean>): DisposableReadOnlyObservableObject<Boolean> {
  return reduce(*other) {
    it.reduce { bool1, bool2 ->
      bool1 && bool2
    }
  }
}
