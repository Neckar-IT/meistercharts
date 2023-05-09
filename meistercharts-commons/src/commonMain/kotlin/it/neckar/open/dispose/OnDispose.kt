package it.neckar.open.dispose

/**
 * Implementations accept the registration of dispose actions
 */
interface OnDispose {
  /**
   * Registers an action that is executed when dispose has been called
   */
  fun onDispose(action: () -> Unit)

  /**
   * Schedules the given disposable to be disposed when dispose has been called
   */
  fun onDispose(disposable: Disposable) {
    onDispose(disposable::dispose)
  }
}
