package it.neckar.open.observable

import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable

/**
 * Base class for observables (which does not have a value!)
 */
open class DefaultObservable<T> : Observable<T>, Disposable, DependentObjectSupport {
  /**
   * The listeners that are notified about changes
   */
  private val valueChangeListeners: MutableList<ConsumeChangesAction<T>> = mutableListOf()

  /**
   * Dependent objects - to avoid premature GC
   */
  private val dependentObjects: DependentObjects = DependentObjects()

  override fun consume(action: ConsumeAction<T>): Disposable {
    return consumeChanges { _, newValue -> action(newValue) }
  }

  override fun consumeChanges(action: ConsumeChangesAction<T>): Disposable {
    valueChangeListeners.add(action)
    return Disposable {
      valueChangeListeners.remove(action)
    }
  }

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

  /**
   * Notifies the listeners about a value change - if the value has changed
   * This method only notifies the listeners when the value has changed
   */
  fun notifyListenersIfChanged(oldValue: T, newValue: T) {
    if (oldValue == newValue) {
      //Nothing has changed, just return
      return
    }

    notifyListeners(oldValue, newValue)
  }

  /**
   * Notifies the listeners - even if [oldValue] and [newValue] are the same
   */
  fun notifyListeners(oldValue: T, newValue: T) {
    valueChangeListeners.fastForEach {
      it(oldValue, newValue)
    }
  }

  /**
   * Disposes the observable object:
   * Removes all listeners
   */
  override fun dispose() {
    valueChangeListeners.clear()
    dependentObjects.dispose()
  }
}
