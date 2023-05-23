package it.neckar.open.observable

import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable
import kotlin.reflect.KProperty

/**
 * An observable object that contains a value and can be observed
 */
open class ObservableObject<T>(initValue: T) : ReadOnlyObservableObject<T> {
  /**
   * The current value
   */
  override var value: T = initValue
    set(value) {
      val oldValue = field
      field = value
      notifyListeners(oldValue, value)
    }

  /**
   * The listeners that are notified about changes
   */
  private val valueChangeListeners: MutableList<ConsumeChangesAction<T>> = mutableListOf()

  /**
   * Dependent objects - to avoid premature GC
   */
  private val dependentObjects: DependentObjects = DependentObjects()

  /**
   * Adds a dependent object that is kept
   */
  override fun addDependentObject(key: Any, dependentObject: Any) {
    dependentObjects[key] = dependentObject
  }

  override fun addDependentObject(dependentObject: Any) {
    dependentObjects[dependentObject] = dependentObject
  }

  /**
   * Returns the dependent object for the given key - if there is one
   */
  override fun getDependentObject(key: Any): Any? {
    return dependentObjects[key]
  }

  /**
   * Removes the dependent object for the given key
   */
  override fun removeDependentObject(key: Any): Any? {
    return dependentObjects.removeDependentObject(key)
  }

  override fun consumeChanges(action: ConsumeChangesAction<T>): Disposable {
    valueChangeListeners.add(action)

    return Disposable { valueChangeListeners.remove(action) }
  }

  override fun consume(immediately: Boolean, action: ConsumeAction<T>): Disposable {
    return consumeChanges { _: T, newValue: T -> action(newValue) }.also {
      if (immediately) {
        action(value)
      }
    }
  }

  /**
   * Notifies the listeners about a value change.
   * This method only notifies the listeners when the value has changed
   */
  private fun notifyListeners(oldValue: T, newValue: T) {
    if (oldValue == newValue) {
      //Nothing has changed, just return
      return
    }

    valueChangeListeners.fastForEach {
      it(oldValue, newValue)
    }
  }

  /**
   * Sets the value - used for delegation to a var:
   * ```
   * val nameProperty = ObservableObject("foo")
   * var name by nameProperty
   * ```
   */
  operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
    this.value = value
  }

  /**
   * Compatibility to JavaFX properties
   */
  fun get(): T {
    return value
  }

  /**
   * Binds this [ObservableObject] to [other] and vice versa.
   *
   * Copies the value from [other] to this initially
   *
   * For a unidirectional binding see [bind]
   */
  fun bindBidirectional(other: ObservableObject<T>) {
    //Copy value from this to other
    consume { newValue -> other.value = newValue }
    other.consumeImmediately { newValue -> this.value = newValue }
  }

  /**
   * Binds two objects bidirectional - using converters.
   *
   * Assigns the value of the other observable to this initially.
   *
   * ATTENTION: The converter must work bidirectional - they must return objects that are equal to each other
   * @param R the other type
   */
  fun <R> bindBidirectional(
    other: ObservableObject<R>,
    converterForward: (newValueToConvert: T, oldConvertedValue: R) -> R,
    converterBack: (newValueToConvert: R, oldConvertedValue: T) -> T
  ) {
    //Copy value from this to other
    var updating = false

    consume { newValue ->
      if (!updating) {
        updating = true
        try {
          other.value = converterForward(newValue, other.value)
        } finally {
          updating = false
        }
      }
    }
    other.consumeImmediately { newValue ->
      if (!updating) {
        updating = true
        try {
          this.value = converterBack(newValue, this.value)
        } finally {
          updating = false
        }
      }
    }
  }

  /**
   * Binds this [ObservableObject] to [other].
   *
   * Copies the value from [other] to this initially
   *
   * For a bidirectional binding see [bindBidirectional]
   */
  fun bind(other: ReadOnlyObservableObject<T>) {
    other.consumeImmediately { newValue -> this.value = newValue }
  }

  override fun toString(): String {
    return "ObservableObject [value: $value]"
  }

  /**
   * Gets the values and applies the new value returned by the lambda
   */
  inline fun getAndSet(function: (oldValue: T) -> T) {
    value = function(value)
  }
}

/**
 * Connects multiple observables using and
 */
fun List<ReadOnlyObservableObject<Boolean>>.and(): ReadOnlyObservableObject<Boolean> {
  require(!isEmpty()) {
    "and must not be called on empty list"
  }
  return reduce {
    it.reduce { bool1, bool2 ->
      bool1 && bool2
    }
  }
}

/**
 * Connects multiple observables using or
 */
fun List<ReadOnlyObservableObject<Boolean>>.or(): ReadOnlyObservableObject<Boolean> {
  require(!isEmpty()) {
    "or must not be called on empty list"
  }
  return reduce {
    it.reduce { bool1, bool2 ->
      bool1 || bool2
    }
  }
}

/**
 * Clears the value
 */
fun <T> ObservableObject<T?>.clear() {
  this.value = null
}
