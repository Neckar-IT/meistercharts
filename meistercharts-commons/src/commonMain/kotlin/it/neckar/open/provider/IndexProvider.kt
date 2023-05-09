package it.neckar.open.provider

import kotlin.jvm.JvmStatic

/**
 * Provides an index for an index.
 *
 * ATTENTION: It is necessary to copy this interface for value classes - to avoid boxing
 * This is just a template class - do *NOT* use
 */
@Deprecated("use copies that use the value classes instead")
interface IndexProvider : HasSize {
  /**
   * Retrieves the index at the given [index].
   *
   * @param index a value between 0 (inclusive) and [size] (exclusive)
   */
  fun valueAt(index: Int): Int

  companion object {
    /**
     * An empty provider that does not return any values
     */
    fun empty(): IndexProvider {
      return empty
    }

    private val empty: IndexProvider = object : IndexProvider {
      override fun size(): Int = 0

      override fun valueAt(index: Int): Int {
        throw UnsupportedOperationException("Must not be called")
      }
    }

    /**
     * Creates a new [IndexProvider] that returns the given values
     */
    @JvmStatic
    fun forValues(vararg values: Int): IndexProvider {
      return object : IndexProvider {
        override fun valueAt(index: Int): Int {
          return values[index]
        }

        override fun size(): Int {
          return values.size
        }
      }
    }

    /**
     * ATTENTION! Use forValues instead (if possible).
     * This method should only be used in very rare cases because of boxing!
     */
    @JvmStatic
    fun forList(values: List<Int>): IndexProvider {
      return object : IndexProvider {
        override fun valueAt(index: Int): Int {
          return values[index]
        }

        override fun size(): Int {
          return values.size
        }
      }
    }
  }
}
