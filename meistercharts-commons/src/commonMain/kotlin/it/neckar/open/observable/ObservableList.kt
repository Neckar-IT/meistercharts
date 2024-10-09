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
   */
  fun <R> mapElements(mapFunction: (T) -> R): ReadOnlyObservableList<R> {
    val mappedInitially = this.value.map { mapFunction(it) }

    return ObservableList(mappedInitially).also {
      consume { newValue ->
        val mapped = newValue.map { mapFunction(it) }
        it.value = mapped
      }
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

  //Register the listeners
  this.consumeImmediately { elements ->
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
  }

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

  //Register the listeners at the observables in the list
  consumeImmediately { elements ->
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
  }

  return resultObservable
}
