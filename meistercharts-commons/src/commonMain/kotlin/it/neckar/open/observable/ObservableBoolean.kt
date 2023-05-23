package it.neckar.open.observable

/**
 * Convenience class; it is the same as [ObservableObject] with type-parameter [Boolean]
 */
class ObservableBoolean(initValue: Boolean) : ObservableObject<Boolean>(initValue), ReadOnlyObservableBoolean {
  /**
   * Toggles the value
   */
  fun toggle() {
    value = !value
  }

  /**
   * Creates a new instance with a default value of `false`
   */
  constructor() : this(
    false
  )
}

/**
 * Convenience class; it is the same as [ReadOnlyObservableObject] with type-parameter [Boolean]
 */
interface ReadOnlyObservableBoolean : ReadOnlyObservableObject<Boolean>
