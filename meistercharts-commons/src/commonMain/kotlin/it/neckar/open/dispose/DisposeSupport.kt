package it.neckar.open.dispose

import it.neckar.open.collections.fastForEach

/**
 * Holds actions that may be called upon dispose
 */
class DisposeSupport(val mode: Mode = Mode.SingleDispose) : Disposable, OnDispose {
  /**
   * The actions that are executed on dispose
   */
  private val disposeActions = mutableListOf<() -> Unit>()

  /**
   * Is set to true if dispose has been called
   */
  var disposed: Boolean = false
    private set

  /**
   * Registers an action that is executed when [dispose] is called
   */
  override fun onDispose(action: () -> Unit) {
    verifyNotDisposed()
    disposeActions.add(action)
  }

  /**
   * Executes all registered (by calling [onDispose]) actions.
   * Marks this as disposed ([disposed])
   */
  override fun dispose() {
    //Copy the dispose actions to avoid endless loops / recursions
    val copy = disposeActions.toList()

    //Clear all actions that have been disposed
    disposeActions.clear()

    copy.fastForEach {
      it()
    }

    disposed = true
  }

  /**
   * Throws an exception, if has already been disposed.
   * Only relevant if the [mode] is set to [Mode.SingleDispose]
   */
  private fun verifyNotDisposed() {
    if (mode == Mode.SingleDispose) {
      check(!disposed) {
        "Already disposed"
      }
    }
  }

  enum class Mode {
    SingleDispose,
    MultiDispose,
  }
}

/**
 * Registers this disposable at the given dispose support
 */
fun <T : Disposable> T.alsoRegisterAt(disposeSupport: DisposeSupport): T {
  disposeSupport.onDispose(this)
  return this
}
