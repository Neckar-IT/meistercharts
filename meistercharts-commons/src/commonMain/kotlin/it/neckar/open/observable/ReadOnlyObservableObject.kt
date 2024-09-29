package it.neckar.open.observable

import it.neckar.open.annotations.JavaFriendly
import it.neckar.open.dispose.Disposable
import kotlin.reflect.KProperty


/**
 * Observable object - read only view
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
   * For a binding to a var look at [it.neckar.open.observable.ObservableObject.setValue(java.lang.Object, kotlin.reflect.KProperty<?>, T)]
   */
  operator fun getValue(thisRef: Any, property: KProperty<*>): T {
    return value
  }

  /**
   * Maps the value of the current observable object to another value
   */
  fun <R> map(function: (T) -> R): ReadOnlyObservableObject<R> {
    val intermediateObservable = ObservableObject(function(value))

    consume { newValue ->
      intermediateObservable.value = function(newValue)
    }
    return intermediateObservable
  }

  fun mapBoolean(function: (T) -> Boolean): ReadOnlyObservableBoolean {
    val intermediateObservable = ObservableBoolean(function(value))

    consume { newValue ->
      intermediateObservable.value = function(newValue)
    }
    return intermediateObservable
  }

  /**
   * Use this observable and another observable and map these two values into a new observable
   */
  fun <U, R> map(otherObservable: ReadOnlyObservableObject<U>, function: (T, U) -> R): ReadOnlyObservableObject<R> {
    val intermediateObservable = ObservableObject(function(value, otherObservable.value))

    consume { newValue ->
      intermediateObservable.value = function(newValue, otherObservable.value)
    }
    otherObservable.consume { newValue ->
      intermediateObservable.value = function(value, newValue)
    }

    return intermediateObservable
  }
}

/**
 * Use this observable and two other observables to create a new value
 */
fun <T, U, V, R> ReadOnlyObservableObject<T>.map(
  otherObservable1: ReadOnlyObservableObject<U>,
  otherObservable2: ReadOnlyObservableObject<V>, function: (T, U, V) -> R,
): ReadOnlyObservableObject<R> {
  val intermediateObservable = ObservableObject(function(value, otherObservable1.value, otherObservable2.value))

  consume { newValue ->
    intermediateObservable.value = function(newValue, otherObservable1.value, otherObservable2.value)
  }
  otherObservable1.consume { newValue ->
    intermediateObservable.value = function(value, newValue, otherObservable2.value)
  }
  otherObservable2.consume { newValue ->
    intermediateObservable.value = function(value, otherObservable1.value, newValue)
  }

  return intermediateObservable
}

/**
 * Reduces this observable with the other observables into a single observable
 */
fun <T, R> ReadOnlyObservableObject<T>.reduce(vararg otherObservables: ReadOnlyObservableObject<T>, function: (List<T>) -> R): ReadOnlyObservableObject<R> {
  val otherObservables1 = listOf(this, *otherObservables)
  return reduceObservables(otherObservables1, function)
}


/**
 * Creates a new observable boolean that holds "or"
 */
infix fun ReadOnlyObservableObject<Boolean>.or(other: ReadOnlyObservableObject<Boolean>): ReadOnlyObservableObject<Boolean> {
  return map(other) { myValue, otherValue ->
    return@map myValue || otherValue
  }
}

/**
 * Creates a new observable boolean that holds "and"
 */
infix fun ReadOnlyObservableObject<Boolean>.and(other: ObservableObject<Boolean>): ReadOnlyObservableObject<Boolean> {
  return map(other) { myValue, otherValue ->
    return@map myValue && otherValue
  }
}

/**
 * Connects multiple observables using or
 */
fun ReadOnlyObservableObject<Boolean>.or(vararg other: ReadOnlyObservableObject<Boolean>): ReadOnlyObservableObject<Boolean> {
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
fun ReadOnlyObservableObject<Boolean>.and(vararg other: ObservableObject<Boolean>): ReadOnlyObservableObject<Boolean> {
  return reduce(*other) {
    it.reduce { bool1, bool2 ->
      bool1 && bool2
    }
  }
}
