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
 * This interface only provides the ability to observe changes. It does not provide the ability to access the current value.
 */
interface Observable<out T> {

  /**
   * Registers an action that is called when the value is changed.
   * This action is called initially if [immediately] is true (default is set to false).
   * @return a dispose action to unregister the given action
   */
  fun consume(immediately: Boolean = false, action: ConsumeAction<T>): Disposable

  /**
   * Registers an action that is called immediately and when the value is changed
   */
  fun consumeImmediately(action: ConsumeAction<T>): Disposable {
    return consume(immediately = true, action = action)
  }

  /**
   * Registers an action that is called when the value has changed. The given action also gets the old value
   * @return a dispose action to unregister the given action
   */
  fun consumeChanges(immediately: Boolean = false, action: ConsumeChangesAction<T>): Disposable

  fun consumeChangesImmediately(action: ConsumeChangesAction<T>): Disposable {
    return consumeChanges(immediately = true, action = action)
  }

  /**
   * Listener that is notified about value changes
   */
  @JavaFriendly
  fun interface ChangeListener<in T> {
    fun valueChanged(oldValue: T, newValue: T)
  }

  @JavaFriendly
  fun addChangeListener(listener: ChangeListener<T>): Disposable {
    return consumeChanges(false, listener::valueChanged)
  }
}
