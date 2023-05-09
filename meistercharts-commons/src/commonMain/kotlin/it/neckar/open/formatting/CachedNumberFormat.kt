package it.neckar.open.formatting

import it.neckar.open.collections.cache
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig

/**
 * A format that returns cached values.
 *
 * This is a tagging interface to ensure a cached format is used
 *
 */
interface CachedNumberFormat : NumberFormat {
  /**
   * Returns the current cache size
   */
  val currentCacheSize: Int
}

/**
 * A format that caches the results
 */
class DefaultCachedFormat internal constructor(
  val format: NumberFormat,
  /**
   * The maximum size of the cache
   */
  val cacheSize: Int = 500,

  /**
   * The hash function that is used to calculate the hash for the current value.
   * This method can *also* use external variables (e.g. a locale or another configuration).
   */
  val hashFunction: (value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig) -> Int = defaultHashFunction,

  ) : CachedNumberFormat {

  init {
    require(format !is CachedNumberFormat) { "cannot cache an already cached number format" }
  }

  /**
   * The cache for the "normal" formatted strings
   */
  private val formatCache = cache<Int, String>("DefaultCachedFormat", cacheSize)

  /**
   * Returns the size of the cache
   */
  override val currentCacheSize: Int
    get() = formatCache.size

  override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    //Calculate the hash code to avoid instantiation of objects
    val key = hashFunction(value, i18nConfiguration, whitespaceConfig)

    return formatCache.getOrStore(key) {
      format.format(value, i18nConfiguration, whitespaceConfig)
    }
  }

  override val precision: Double
    get() = format.precision

  companion object {
    /**
     * The hash function that is used per default
     */
    val defaultHashFunction: (value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig) -> Int = { value, i18nConfiguration, spaceConfig ->
      31 * value.hashCode() + i18nConfiguration.hashCode() + spaceConfig.hashCode()
    }
  }
}

/**
 * Caches the results of the number format
 */
fun NumberFormat.cached(
  cacheSize: Int = 500,
  /**
   * Calculates an additional hash that will be added to the hash of the value.
   * Can be used to create a unique hash for other (external) factors: For example a unit
   */
  additionalHashFunction: (() -> Int)? = null
): CachedNumberFormat {
  //Create a custom hash function - only if a additionalHashFunction is provided
  val hashFunction = additionalHashFunction?.let {
    //Create a custom hash function
    { value, i18nConfiguration, spaceConfig ->
      DefaultCachedFormat.defaultHashFunction(value, i18nConfiguration, spaceConfig) + additionalHashFunction()
    }
  } ?: DefaultCachedFormat.defaultHashFunction //Fallback to the default hash function

  return DefaultCachedFormat(this, cacheSize = cacheSize, hashFunction = hashFunction)
}

/**
 * Helper method to avoid unnecessary calls to cached
 */
@Suppress("UNUSED_PARAMETER")
@Deprecated("Do not cache a cached format", ReplaceWith("this"), level = DeprecationLevel.ERROR)
fun CachedNumberFormat.cached(
  cacheSize: Int = 500,
  hashFunction: (value: Double, i18nConfiguration: I18nConfiguration) -> Int = { _, _ -> throw UnsupportedOperationException("must not be called!") }
): CachedNumberFormat {
  return this
}

