package it.neckar.open.provider

import it.neckar.open.annotations.Boxed
import it.neckar.open.annotations.NotBoxed
import kotlin.reflect.KProperty0

/**
 * Provides boolean values
 */
interface BooleanValuesProvider : HasSize {
  /**
   * Retrieves the value at the given [index].
   *
   * @param index a value between 0 (inclusive) and [size] (exclusive)
   */
  fun valueAt(index: Int): @NotBoxed Boolean
}

/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun KProperty0<BooleanValuesProvider>.delegate(): BooleanValuesProvider {
  return object : BooleanValuesProvider {
    override fun size(): Int = get().size()

    override fun valueAt(index: Int): @Boxed Boolean {
      return get().valueAt(index)
    }
  }
}
