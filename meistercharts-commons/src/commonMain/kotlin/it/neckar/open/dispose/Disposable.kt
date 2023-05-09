package it.neckar.open.dispose

import it.neckar.open.collections.fastForEach

/**
 * An action that can be invoked to dispose something
 */
fun interface Disposable {
  /**
   * Disposes the element
   */
  fun dispose()

  companion object {
    /**
     * Combines multiple disposable into on
     */
    fun all(vararg disposables: Disposable): Disposable {
      if (disposables.isEmpty()) {
        return noop
      }

      return Disposable {
        disposables.fastForEach { it.dispose() }
      }
    }

    /**
     * Does nothing
     */
    val noop: Disposable = Disposable { }
  }
}
