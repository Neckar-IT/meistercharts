package it.neckar.open.observable

/**
 * Convenience class; it is the same as [ObservableObject] with type-parameter [Int]
 */
class ObservableInt(initValue: Int) : ObservableObject<Int>(initValue), ReadOnlyObservableInt

/**
 * Convenience class; it is the same as [ReadOnlyObservableObject] with type-parameter [Int]
 */
interface ReadOnlyObservableInt : ReadOnlyObservableObject<Int> {
  /**
   * Creates a binding that compares the value of this to the given value
   */
  fun isEqualTo(compareWith: Int): ReadOnlyObservableObject<out Boolean> {
    val intermediateObservable = ObservableBoolean(this.value == compareWith)

    consume { newValue ->
      intermediateObservable.value = newValue == compareWith
    }

    return intermediateObservable
  }
}


/**
 * Converts the observable object to an observable object that holds a number
 */
fun ObservableObject<Int>.toNumber(): ObservableObject<Number> {
  val numberObservableObject = ObservableObject<Number>(this.value)

  consume {
    numberObservableObject.value = it
  }
  numberObservableObject.consume {
    this.value = it.toInt()
  }

  return numberObservableObject
}

/**
 * Converts the observable object to an observable object that holds a number
 */
fun ObservableObject<Int>.toDouble(): ObservableObject<Double> {
  val numberObservableObject = ObservableDouble(value.toDouble())

  consume {
    numberObservableObject.value = it.toDouble()
  }
  numberObservableObject.consume {
    this.value = it.toInt()
  }

  return numberObservableObject
}
