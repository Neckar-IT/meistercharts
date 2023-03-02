package com.meistercharts.canvas.layout.cache

/**
 * A cache for [LayoutVariable]s.
 * These will be reused for every layout pass.
 *
 * ATTENTION: This cache might contain more objects than requested!
 */
class LayoutVariableObjectCache<T : LayoutVariable>(
  /**
   * The factory that is used to create new elements.
   *
   * ATTENTION: The factory is only called once for each index.
   * The created objects are reused for each layout pass afterwards.
   */
  factory: () -> T
) : AbstractLayoutVariableObjectCache<T>(factory) {

  override fun reset() {
    //only reset the values - that list has the correct size
    //objectsStock might have additional objects - these are not used, therefore can be ignored safely
    values.forEachIndexed { index, _ ->
      values[index].reset()
    }
  }

}
