package it.neckar.open.i18n

import kotlinx.serialization.Serializable
import kotlin.contracts.contract
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * A unique key to identify a certain piece of text
 */
@Serializable
class TextKey(
  /**
   * The key
   */
  val key: String,
  /**
   * The fallback text that may be used if no resolved text is available
   */
  val fallbackText: String
) {

  override fun toString(): String {
    return "$key [$fallbackText]"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TextKey) return false

    if (key != other.key) return false

    return true
  }

  override fun hashCode(): Int {
    return key.hashCode()
  }

  /**
   * Returns true if the key is an empty string
   */
  fun isEmpty(): Boolean {
    return this.key == ""
  }

  companion object {
    /**
     * Creates a new text key with the given default text that is also used as key
     */
    @JvmStatic
    fun simple(keyAndFallbackText: String): TextKey {
      return TextKey(keyAndFallbackText, keyAndFallbackText)
    }

    /**
     * Creates a text key where the key and fallback text are set to the provided value
     */
    operator fun invoke(keyAndFallbackText: String): TextKey {
      return simple(keyAndFallbackText)
    }

    /**
     * A [TextKey] for the empty string
     */
    @JvmField
    val empty: TextKey = simple("")
  }
}

/**
 * Returns false, if this is null
 */
inline fun TextKey?.isEmpty(): Boolean {
  contract {
    returns(false) implies (this@isEmpty != null)
  }
  return this?.isEmpty() ?: true
}
