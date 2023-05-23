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
fun <T, N> ObservableObject<T>.select(extractNested: (T) -> ReadOnlyObservableObject<N>): ReadOnlyObservableObject<N> {
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
