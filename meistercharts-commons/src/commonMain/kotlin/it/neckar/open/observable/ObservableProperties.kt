package it.neckar.open.observable

import it.neckar.open.dispose.Disposable

/**
 * Interface to observe changes of all/multiple properties within a class.
 *
 * Note: It does not make sense to have the `*Immediately` methods here, because they are not applicable to all properties.
 */
interface ObservableProperties : Observable<Any?> {
  /**
   * Registers a listener notified when any property changes.
   */
  fun consumeAllProperties(action: ConsumeAction<Any?>): Disposable {
    return consumeAllPropertiesChanges { _, newValue ->
      action(newValue)
    }
  }

  fun consumeAllPropertiesChanges(action: ConsumeChangesAction<Any?>): Disposable

  override fun consume(immediately: Boolean, action: ConsumeAction<Any?>): Disposable {
    require(immediately.not()) { "immediately is not supported" }
    return consumeAllProperties(action)
  }

  override fun consumeChanges(immediately: Boolean, action: ConsumeChangesAction<Any?>): Disposable {
    require(immediately.not()) { "immediately is not supported" }
    return consumeAllPropertiesChanges(action)
  }
}
