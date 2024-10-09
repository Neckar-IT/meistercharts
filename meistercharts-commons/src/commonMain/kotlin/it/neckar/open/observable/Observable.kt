package it.neckar.open.observable

import it.neckar.open.annotations.JavaFriendly
import it.neckar.open.dispose.Disposable

/**
 * Consumes new values (but does not receive the old one)
 */
typealias ConsumeAction<T> = (newValue: T) -> Unit

/**
 * Consumes changes - the old and new value is provided
 */
typealias ConsumeChangesAction<T> = (oldValue: T, newValue: T) -> Unit


/**
 * Represents an observable object.
 *
 * This interface only provides the ability to observe changes.
 * It does not provide the ability to access the current value.
 *
 * If the current value is required, use [ReadOnlyObservableObject] instead.
 */
interface Observable<out T> {
  /**
   * Registers an action that will be called when the value is changed.
   * @return a dispose action to unregister the given action
   */
  fun consume(action: ConsumeAction<T>): Disposable

  /**
   * Registers an action that will be called when the value has changed. The given action also gets the old value (if available)
   * @return a dispose action to unregister the given action
   */
  fun consumeChanges(action: ConsumeChangesAction<T>): Disposable


  /**
   * Listener that is notified about value changes
   */
  @JavaFriendly
  fun interface ChangeListener<in T> {
    fun valueChanged(oldValue: T, newValue: T)
  }

  /**
   * Registers a change listener that is notified about value changes
   */
  @JavaFriendly
  fun addChangeListener(listener: ChangeListener<T>): Disposable {
    return consumeChanges(listener::valueChanged)
  }
}
