package it.neckar.open.observable

/**
 * Convenience class; it is the same as [ObservableObject] with type-parameter [Double]
 */
class ObservableDouble(initValue: Double) : ObservableObject<Double>(initValue), ReadOnlyObservableDouble

/**
 * Convenience class; it is the same as [ReadOnlyObservableObject] with type-parameter [Double]
 */
interface ReadOnlyObservableDouble : ReadOnlyObservableObject<Double> {

  /**
   * Creates a binding that compares the value of this to the given value
   */
  fun isEqualTo(compareWith: Double): ReadOnlyObservableObject<out Boolean> {
    val intermediateObservable = ObservableBoolean(this.value == compareWith)

    consume { newValue ->
      intermediateObservable.value = newValue == compareWith
    }

    return intermediateObservable
  }
}
