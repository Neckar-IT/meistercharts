package com.meistercharts.canvas

/**
 * Supports the dirty state
 */
class DirtySupport {
  /**
   * Whether we are dirty or not.
   */
  var dirty: Boolean = false
    private set

  /**
   * Sets the dirty-state to `true`
   * @see [clearIsDirty]
   */
  fun markAsDirty() {
    dirty = true
  }

  /**
   * Sets the dirty-state to `false`
   * @see [markAsDirty]
   */
  fun clearIsDirty() {
    dirty = false
  }

  /**
   * Calls the given function if dirty state is `true`
   */
  inline fun ifDirty(function: () -> Unit) {
    if (!dirty) {
      return
    }
    clearIsDirty()

    function()
  }
}
