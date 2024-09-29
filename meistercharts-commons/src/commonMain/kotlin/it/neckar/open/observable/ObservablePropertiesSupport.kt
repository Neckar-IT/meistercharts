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
        valueChangeListeners.fastForEach { listener ->
          listener(old, new)
        }
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
    return Disposable { valueChangeListeners.remove(action) }
  }

  companion object {
    operator fun invoke(vararg properties: Observable<*>): ObservablePropertiesSupport {
      return ObservablePropertiesSupport(properties.toList())
    }

    operator fun invoke(vararg properties: ObservableProperties): ObservablePropertiesSupport {
      return ObservablePropertiesSupport(
        properties.toList()
      )
    }
  }
}
