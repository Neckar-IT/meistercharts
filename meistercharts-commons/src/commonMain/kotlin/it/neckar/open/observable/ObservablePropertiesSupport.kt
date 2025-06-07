package it.neckar.open.observable

import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport

/**
 * Supports observing changes of multiple properties within a class.
 */
open class ObservablePropertiesSupport(
  properties: List<Observable<*>>,
) : ObservableProperties, Disposable {

  private val disposeSupport = DisposeSupport()

  private val valueChangeListeners: MutableList<ConsumeChangesAction<Any?>> = mutableListOf()

  init {
    properties.fastForEach { property ->
      property.consumeChanges { old, new ->
        notifyChange(old, new)
      }.also {
        disposeSupport.onDispose(it)
      }
    }
  }

  override fun dispose() {
    disposeSupport.dispose()
  }

  override fun consumeAllPropertiesChanges(action: ConsumeChangesAction<Any?>): Disposable {
    valueChangeListeners.add(action)
    return Disposable {
      valueChangeListeners.remove(action)
    }
  }

  /**
   * Notifies all listeners about a change.
   */
  fun notifyChange(oldValue: Any?, newValue: Any?) {
    valueChangeListeners.fastForEach { listener ->
      listener(oldValue, newValue)
    }
  }

  companion object {
    /**
     * Creates a new [ObservablePropertiesSupport] instance with the given properties.
     */
    operator fun invoke(vararg properties: Observable<*>): ObservablePropertiesSupport {
      return ObservablePropertiesSupport(properties.toList())
    }

    /**
     * Creates a new [ObservablePropertiesSupport] instance with the given properties.
     */
    operator fun invoke(vararg properties: ObservableProperties): ObservablePropertiesSupport {
      return ObservablePropertiesSupport(
        properties.toList()
      )
    }

    /**
     * Builds a new [ObservablePropertiesSupport] instance.
     */
    fun create(action: Builder.() -> Unit): ObservablePropertiesSupport {
      val builder = Builder()
      builder.action()
      return builder.build()
    }
  }

  /**
   * Builder for [ObservablePropertiesSupport].
   */
  class Builder() {
    /**
     * The list of observables that should be observed.
     */
    val observables: MutableList<Observable<*>> = mutableListOf()

    fun build(): ObservablePropertiesSupport {
      return ObservablePropertiesSupport(observables)
    }

    @Deprecated("Use observableList.selectListObservable or addObservableListNoSelected instead", level = DeprecationLevel.ERROR)
    fun addObservable(observableList: ObservableList<*>) {
      addObservable(observableList as Observable<*>)
    }

    /**
     * Adds an observable list to the support.
     * Automatically registers the listeners for all elements in the list.
     */
    fun <T> addObservableListSelected(observableList: ObservableList<T>, extractNestedObservable: (T) -> Observable<Any?>) {
      this.observables.add(observableList.selectListObservable(extractNestedObservable))
    }

    fun <T> addObservableListNoSelected(observableList: ObservableList<T>) {
      this.observables.add(observableList)
    }

    /**
     * Adds a single observable to the support
     */
    fun addObservable(observable: Observable<*>) {
      this.observables.add(observable)
    }
  }
}
